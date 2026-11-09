package com.tienda.backend.venta.application.service;

import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import com.tienda.backend.venta.application.model.ResultadoRegistroVenta;
import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.venta.application.model.VentaVista;
import com.tienda.backend.venta.application.port.in.AnularVentaUseCase;
import com.tienda.backend.venta.application.port.in.ConsultarVentaUseCase;
import com.tienda.backend.venta.application.port.in.RegistrarVentaUseCase;
import com.tienda.backend.venta.application.port.out.VentaRepositoryPort;
import com.tienda.backend.venta.domain.exception.VentaNotFoundException;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Casos de uso de ventas. A propósito NO es transaccional: crear y anular se ejecutan en {@link VentaEjecutor}
 * (una transacción con los bloqueos) y recién después de confirmarse se consultan los nombres para la nota de venta.
 */
@Service
@RequiredArgsConstructor
public class VentaService implements RegistrarVentaUseCase, AnularVentaUseCase, ConsultarVentaUseCase {

    /** Permiso para ver las ventas de todos (debe coincidir con el de la tabla permisos). */
    private static final String PERMISO_VER_TODAS = "VENTA_VER";

    private final VentaEjecutor ejecutor;
    private final VentaVistaFactory vistaFactory;
    private final VentaRepositoryPort ventaRepositoryPort;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    public ResultadoRegistroVenta registrar(RegistrarVentaComando comando) {
        try {
            VentaEjecutor.Registrada r = ejecutor.registrar(comando);
            return new ResultadoRegistroVenta(vistaFactory.construir(r.venta()), r.creada(), r.creditoAplicado());
        } catch (DataIntegrityViolationException e) {
            // Dos peticiones con la misma clave a la vez: el índice único rechazó a la segunda; gana la que se guardó
            String clave = comando.idempotencyKey();
            if (clave != null) {
                Optional<Venta> ganadora = ventaRepositoryPort.findByUsuarioIdAndIdempotencyKey(usuarioActualPort.id(), clave);
                if (ganadora.isPresent()) {
                    return new ResultadoRegistroVenta(vistaFactory.construir(ganadora.get()), false);
                }
            }
            throw e;
        }
    }

    @Override
    public VentaVista anular(Long ventaId, String motivo) {
        return vistaFactory.construir(ejecutor.anular(ventaId, motivo));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaVista obtener(Long ventaId) {
        Venta venta = ventaRepositoryPort.findById(ventaId).orElseThrow(() -> new VentaNotFoundException(ventaId));
        if (!venta.esDe(usuarioActualPort.id()) && !usuarioActualPort.tienePermiso(PERMISO_VER_TODAS)) {
            throw new AccesoDenegadoException("No puede ver una venta registrada por otro usuario.");
        }
        return vistaFactory.construir(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Venta> listar(VentaFiltro filtro, int page, int size) {
        // Quien no puede ver todas solo ve las suyas, pida lo que pida
        VentaFiltro efectivo = usuarioActualPort.tienePermiso(PERMISO_VER_TODAS) ? filtro
                : new VentaFiltro(filtro.numero(), filtro.turnoId(), filtro.clienteId(), usuarioActualPort.id(),
                        filtro.estado(), filtro.desde(), filtro.hasta());
        return ventaRepositoryPort.findAll(efectivo, PageResult.safePage(page), PageResult.safeSize(size));
    }
}
