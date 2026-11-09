package com.tienda.backend.presentacion.application.service;

import com.tienda.backend.presentacion.application.model.EditarPresentacionComando;
import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.application.port.in.CrearPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.EditarPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.EliminarPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.ListarPresentacionesPorProductoUseCase;
import com.tienda.backend.presentacion.application.port.in.ObtenerPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import com.tienda.backend.presentacion.application.port.out.ProductoConsultaPort;
import com.tienda.backend.presentacion.application.port.out.UnidadMedidaConsultaPort;
import com.tienda.backend.presentacion.domain.exception.PresentacionBaseNoEliminableException;
import com.tienda.backend.presentacion.domain.exception.PresentacionBaseYaExisteException;
import com.tienda.backend.presentacion.domain.exception.PresentacionCodigoBarrasDuplicadoException;
import com.tienda.backend.presentacion.domain.exception.PresentacionDatosInvalidosException;
import com.tienda.backend.presentacion.domain.exception.PresentacionNotFoundException;
import com.tienda.backend.presentacion.domain.model.Presentacion;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.unidadmedida.domain.exception.UnidadMedidaNotFoundException;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresentacionService implements CrearPresentacionUseCase, EditarPresentacionUseCase,
        ListarPresentacionesPorProductoUseCase, ObtenerPresentacionUseCase, EliminarPresentacionUseCase {

    private final PresentacionRepositoryPort presentacionRepositoryPort;
    private final ProductoConsultaPort productoConsultaPort;
    private final UnidadMedidaConsultaPort unidadMedidaConsultaPort;

    @Override
    @Transactional
    public PresentacionDetalle create(Presentacion presentacion) {
        Long productoId = presentacion.getProductoId();
        validarProducto(productoId);
        UnidadMedida unidad = buscarUnidad(presentacion.getUnidadMedidaId());

        if (presentacionRepositoryPort.existsByCodigoBarras(presentacion.getCodigoBarras())) {
            throw new PresentacionCodigoBarrasDuplicadoException(presentacion.getCodigoBarras());
        }

        boolean tieneBase = presentacionRepositoryPort.existsBaseByProductoId(productoId);
        if (!tieneBase && !presentacion.isEsUnidadBase()) {
            throw new PresentacionDatosInvalidosException(
                    "La primera presentación del producto debe ser la unidad base");
        }
        if (tieneBase && presentacion.isEsUnidadBase()) {
            throw new PresentacionBaseYaExisteException(productoId);
        }

        return detalle(presentacionRepositoryPort.save(presentacion), unidad);
    }

    @Override
    @Transactional
    public PresentacionDetalle editar(Long id, EditarPresentacionComando comando) {
        Presentacion existente = buscar(id);
        UnidadMedida unidad = buscarUnidad(comando.unidadMedidaId());

        String codigo = comando.codigoBarras() == null ? null : comando.codigoBarras().trim();
        if (codigo != null && presentacionRepositoryPort.existsByCodigoBarrasAndIdNot(codigo, id)) {
            throw new PresentacionCodigoBarrasDuplicadoException(codigo);
        }

        existente.editar(comando.unidadMedidaId(), comando.codigoBarras(), comando.factorConversion());
        return detalle(presentacionRepositoryPort.save(existente), unidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PresentacionDetalle> listarPorProducto(Long productoId) {
        validarProducto(productoId);
        return presentacionRepositoryPort.findDetalleByProductoId(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public PresentacionDetalle obtener(Long id) {
        Presentacion presentacion = buscar(id);
        return detalle(presentacion, buscarUnidad(presentacion.getUnidadMedidaId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PresentacionDetalle obtenerPorCodigoBarras(String codigoBarras) {
        String codigo = codigoBarras == null ? "" : codigoBarras.trim();
        return presentacionRepositoryPort.findDetalleByCodigoBarras(codigo)
                .orElseThrow(() -> new PresentacionNotFoundException(codigo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Presentacion presentacion = buscar(id);
        if (presentacion.isEsUnidadBase()
                && presentacionRepositoryPort.countByProductoId(presentacion.getProductoId()) > 1) {
            throw new PresentacionBaseNoEliminableException();
        }
        presentacionRepositoryPort.deleteById(id);
    }

    private Presentacion buscar(Long id) {
        return presentacionRepositoryPort.findById(id)
                .orElseThrow(() -> new PresentacionNotFoundException(id));
    }

    private UnidadMedida buscarUnidad(Long unidadMedidaId) {
        return unidadMedidaConsultaPort.obtenerPorId(unidadMedidaId)
                .orElseThrow(() -> new UnidadMedidaNotFoundException(unidadMedidaId));
    }

    private void validarProducto(Long productoId) {
        if (!productoConsultaPort.existePorId(productoId)) {
            throw new ProductoNotFoundException(productoId);
        }
    }

    private PresentacionDetalle detalle(Presentacion presentacion, UnidadMedida unidad) {
        return new PresentacionDetalle(presentacion, unidad.getNombre(), unidad.getAbreviatura());
    }
}
