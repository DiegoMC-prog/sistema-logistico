package com.tienda.backend.lote.application.service;

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
import com.tienda.backend.lote.application.port.out.LoteRepositoryPort;
import com.tienda.backend.lote.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.lote.application.port.out.ProductoConsultaPort;
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
        ObtenerLoteUseCase, EliminarLoteUseCase {

    private final LoteRepositoryPort loteRepositoryPort;
    private final ProductoConsultaPort productoConsultaPort;
    private final PresentacionConsultaPort presentacionConsultaPort;
    private final GeneradorNumeroLotePort generadorNumeroLotePort;

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

        existente.editar(comando.numeroLote(), comando.fechaVencimiento(),
                comando.costoUnitarioCompra(), comando.porcentajeDescuentoVencimiento());
        return new LoteDetalle(loteRepositoryPort.save(existente), producto.getNombre());
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
}
