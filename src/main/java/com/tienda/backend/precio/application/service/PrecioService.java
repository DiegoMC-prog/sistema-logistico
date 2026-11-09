package com.tienda.backend.precio.application.service;

import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.application.model.PrecioFiltro;
import com.tienda.backend.precio.application.model.TipoClienteRef;
import com.tienda.backend.precio.application.port.in.CambiarEstadoPrecioUseCase;
import com.tienda.backend.precio.application.port.in.CrearPrecioUseCase;
import com.tienda.backend.precio.application.port.in.EditarPrecioUseCase;
import com.tienda.backend.precio.application.port.in.EliminarPrecioUseCase;
import com.tienda.backend.precio.application.port.in.ListarPreciosUseCase;
import com.tienda.backend.precio.application.port.in.ObtenerPrecioUseCase;
import com.tienda.backend.precio.application.port.in.ResolverPrecioUseCase;
import com.tienda.backend.precio.application.port.out.PrecioRepositoryPort;
import com.tienda.backend.precio.application.port.out.PrecioTipoClienteConsultaPort;
import com.tienda.backend.precio.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.precio.domain.exception.PrecioDatosInvalidosException;
import com.tienda.backend.precio.domain.exception.PrecioDuplicadoException;
import com.tienda.backend.precio.domain.exception.PrecioNoDisponibleException;
import com.tienda.backend.precio.domain.exception.PrecioNotFoundException;
import com.tienda.backend.precio.domain.exception.PrecioTipoClienteInactivoException;
import com.tienda.backend.precio.domain.model.Precio;
import com.tienda.backend.presentacion.domain.exception.PresentacionNotFoundException;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrecioService implements CrearPrecioUseCase, EditarPrecioUseCase, ListarPreciosUseCase,
        ObtenerPrecioUseCase, EliminarPrecioUseCase, CambiarEstadoPrecioUseCase, ResolverPrecioUseCase {

    private final PrecioRepositoryPort precioRepositoryPort;
    private final PresentacionConsultaPort presentacionConsultaPort;
    private final PrecioTipoClienteConsultaPort tipoClienteConsultaPort;

    @Override
    @Transactional
    public PrecioDetalle create(Long presentacionId, Precio precio) {
        exigirPresentacion(presentacionId);
        String tipoNombre = null;
        if (precio.getTipoClienteId() != null) {
            TipoClienteRef tipo = tipoClienteConsultaPort.buscarPorId(precio.getTipoClienteId())
                    .orElseThrow(() -> new TipoClienteNotFoundException(precio.getTipoClienteId()));
            if (!tipo.activo()) {
                throw new PrecioTipoClienteInactivoException(tipo.nombre());
            }
            tipoNombre = tipo.nombre();
        }
        if (precioRepositoryPort.existsByClave(presentacionId, precio.getTipoClienteId(), precio.getCantidadMinima())) {
            throw new PrecioDuplicadoException();
        }
        return new PrecioDetalle(precioRepositoryPort.save(precio), tipoNombre);
    }

    @Override
    @Transactional
    public PrecioDetalle editar(Long id, BigDecimal cantidadMinima, BigDecimal nuevoPrecio) {
        Precio existente = buscar(id);
        existente.editar(cantidadMinima, nuevoPrecio);
        if (precioRepositoryPort.existsByClaveAndIdNot(existente.getPresentacionId(), existente.getTipoClienteId(),
                existente.getCantidadMinima(), id)) {
            throw new PrecioDuplicadoException();
        }
        return detalle(precioRepositoryPort.save(existente));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PrecioDetalle> listar(Long presentacionId, PrecioFiltro filtro, int page, int size) {
        exigirPresentacion(presentacionId);
        PageResult<Precio> pagina = precioRepositoryPort.findByPresentacion(
                presentacionId, filtro, PageResult.safePage(page), PageResult.safeSize(size));
        Map<Long, String> nombres = nombres(pagina.content());
        return pagina.map(p -> new PrecioDetalle(p, p.getTipoClienteId() == null ? null : nombres.get(p.getTipoClienteId())));
    }

    @Override
    @Transactional(readOnly = true)
    public PrecioDetalle obtener(Long id) {
        return detalle(buscar(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        precioRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public PrecioDetalle activar(Long id) {
        Precio precio = buscar(id);
        precio.activar();
        return detalle(precioRepositoryPort.save(precio));
    }

    @Override
    @Transactional
    public PrecioDetalle desactivar(Long id) {
        Precio precio = buscar(id);
        precio.desactivar();
        return detalle(precioRepositoryPort.save(precio));
    }

    /**
     * Regla: si hay precio para el tipo de cliente, gana sobre el general (aunque el general sea más barato).
     * Dentro del grupo elegido se toma el de mayor cantidad mínima alcanzada.
     */
    @Override
    @Transactional(readOnly = true)
    public PrecioDetalle resolver(Long presentacionId, BigDecimal cantidad, Long tipoClienteId) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new PrecioDatosInvalidosException("La cantidad debe ser mayor a cero");
        }
        exigirPresentacion(presentacionId);
        List<Precio> candidatos = precioRepositoryPort.findCandidatos(presentacionId, cantidad, tipoClienteId);
        Comparator<Precio> porEscalon = Comparator.comparing(Precio::getCantidadMinima);

        Precio elegido = candidatos.stream()
                .filter(p -> tipoClienteId != null && tipoClienteId.equals(p.getTipoClienteId()))
                .max(porEscalon)
                .or(() -> candidatos.stream().filter(Precio::esGeneral).max(porEscalon))
                .orElseThrow(() -> new PrecioNoDisponibleException(presentacionId, cantidad));
        return detalle(elegido);
    }

    private Precio buscar(Long id) {
        return precioRepositoryPort.findById(id).orElseThrow(() -> new PrecioNotFoundException(id));
    }

    private void exigirPresentacion(Long presentacionId) {
        if (!presentacionConsultaPort.existe(presentacionId)) {
            throw new PresentacionNotFoundException(presentacionId);
        }
    }

    private PrecioDetalle detalle(Precio precio) {
        String nombre = precio.getTipoClienteId() == null ? null
                : nombres(List.of(precio)).get(precio.getTipoClienteId());
        return new PrecioDetalle(precio, nombre);
    }

    private Map<Long, String> nombres(List<Precio> precios) {
        Set<Long> ids = precios.stream().map(Precio::getTipoClienteId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        return ids.isEmpty() ? Map.of() : tipoClienteConsultaPort.nombresPorIds(ids);
    }
}
