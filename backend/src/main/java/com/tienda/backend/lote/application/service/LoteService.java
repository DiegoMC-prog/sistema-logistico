package com.tienda.backend.lote.application.service;

import com.tienda.backend.lote.application.model.AjusteCostoLote;
import com.tienda.backend.lote.application.port.in.ConsultarAjustesCostoUseCase;
import com.tienda.backend.lote.application.port.out.AjusteCostoLotePort;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import java.math.BigDecimal;
import java.util.List;
import com.tienda.backend.lote.application.model.CrearLoteComando;
import com.tienda.backend.lote.application.model.EditarLoteComando;
import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.lote.application.port.in.CrearLoteUseCase;
import com.tienda.backend.lote.application.port.in.EditarLoteUseCase;
import com.tienda.backend.lote.application.port.in.EliminarLoteUseCase;
import com.tienda.backend.lote.application.port.in.ListarLoteUseCase;
import com.tienda.backend.lote.application.port.in.ObtenerLoteUseCase;
import com.tienda.backend.lote.application.port.out.GeneradorNumeroLotePort;
import com.tienda.backend.lote.application.port.out.InventarioConsultaPort;
import com.tienda.backend.lote.application.port.out.LoteRepositoryPort;
import com.tienda.backend.lote.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.lote.application.port.out.ProductoConsultaPort;
import com.tienda.backend.lote.domain.exception.LoteConStockException;
import com.tienda.backend.lote.domain.exception.LoteDatosInvalidosException;
import com.tienda.backend.lote.domain.exception.LoteNotFoundException;
import com.tienda.backend.lote.domain.exception.LoteNumeroDuplicadoException;
import com.tienda.backend.lote.domain.exception.LoteProductoSinPresentacionBaseException;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LoteService implements CrearLoteUseCase, EditarLoteUseCase, ListarLoteUseCase,
        ObtenerLoteUseCase, EliminarLoteUseCase, ConsultarAjustesCostoUseCase {

    private final LoteRepositoryPort loteRepositoryPort;
    private final ProductoConsultaPort productoConsultaPort;
    private final PresentacionConsultaPort presentacionConsultaPort;
    private final GeneradorNumeroLotePort generadorNumeroLotePort;
    private final InventarioConsultaPort inventarioConsultaPort;
    private final AjusteCostoLotePort ajusteCostoLotePort;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    @Transactional
    public LoteDetalle create(CrearLoteComando comando) {
        Producto producto = buscarProducto(comando.productoId());

        if (!presentacionConsultaPort.existeBasePorProducto(producto.getId())) {
            throw new LoteProductoSinPresentacionBaseException(producto.getId());
        }
        validarFechaVencimiento(producto, comando.fechaVencimiento());
        if (comando.fechaVencimiento() != null && comando.fechaVencimiento().isBefore(LocalDate.now())) {
            throw new LoteDatosInvalidosException("No se puede registrar un lote con fecha de vencimiento anterior a hoy");
        }

        String numeroLote = (comando.numeroLote() == null || comando.numeroLote().isBlank())
                ? generadorNumeroLotePort.siguiente()
                : comando.numeroLote().trim();

        if (loteRepositoryPort.existsByProductoIdAndNumeroLote(producto.getId(), numeroLote)) {
            throw new LoteNumeroDuplicadoException(numeroLote);
        }

        Lote lote = new Lote(producto.getId(), numeroLote, comando.fechaVencimiento(),
                comando.costoUnitarioCompra(), comando.porcentajeDescuentoVencimiento());
        return new LoteDetalle(loteRepositoryPort.save(lote), producto.getNombre());
    }

    @Override
    @Transactional
    public LoteDetalle editar(Long id, EditarLoteComando comando) {
        Lote existente = buscar(id);
        Producto producto = buscarProducto(existente.getProductoId());

        validarFechaVencimiento(producto, comando.fechaVencimiento());

        String numeroLote = comando.numeroLote() == null ? null : comando.numeroLote().trim();
        if (numeroLote != null && loteRepositoryPort.existsByProductoIdAndNumeroLoteAndIdNot(
                producto.getId(), numeroLote, id)) {
            throw new LoteNumeroDuplicadoException(numeroLote);
        }

        BigDecimal costoAnterior = existente.getCostoUnitarioCompra();
        existente.editar(comando.numeroLote(), comando.fechaVencimiento(),
                comando.costoUnitarioCompra(), comando.porcentajeDescuentoVencimiento());

        // R10: el costo solo se corrige con una nota de ajuste (valor anterior, nuevo, motivo y quién lo hizo)
        BigDecimal costoNuevo = existente.getCostoUnitarioCompra();
        boolean cambiaCosto = costoAnterior != null && costoNuevo != null && costoAnterior.compareTo(costoNuevo) != 0;
        String motivo = comando.motivoAjusteCosto() == null ? "" : comando.motivoAjusteCosto().trim();
        if (cambiaCosto && motivo.length() < 5) {
            throw new LoteDatosInvalidosException(
                    "Para corregir el costo del lote indique el motivo del ajuste (al menos 5 caracteres): queda registrado");
        }
        if (motivo.length() > 500) {
            throw new LoteDatosInvalidosException("El motivo del ajuste de costo no debe tener más de 500 caracteres");
        }
        Lote guardado = loteRepositoryPort.save(existente);
        if (cambiaCosto) {
            ajusteCostoLotePort.registrar(id, costoAnterior, costoNuevo, motivo, usuarioActualPort.nombre());
        }
        return new LoteDetalle(guardado, producto.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<LoteDetalle> listar(LoteFiltro filtro, int page, int size) {
        return loteRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public LoteDetalle obtener(Long id) {
        Lote lote = buscar(id);
        return new LoteDetalle(lote, buscarProducto(lote.getProductoId()).getNombre());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (inventarioConsultaPort.tieneStock(id)) {
            throw new LoteConStockException();
        }
        loteRepositoryPort.deleteById(id);
    }

    private Lote buscar(Long id) {
        return loteRepositoryPort.findById(id)
                .orElseThrow(() -> new LoteNotFoundException(id));
    }

    private Producto buscarProducto(Long productoId) {
        return productoConsultaPort.obtenerPorId(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
    }

    private void validarFechaVencimiento(Producto producto, LocalDate fechaVencimiento) {
        if (producto.isEsPerecedero() && fechaVencimiento == null) {
            throw new LoteDatosInvalidosException(
                    "La fecha de vencimiento es obligatoria para productos perecederos");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AjusteCostoLote> ajustesCosto(Long loteId) {
        buscar(loteId);
        return ajusteCostoLotePort.deLote(loteId);
    }
}
