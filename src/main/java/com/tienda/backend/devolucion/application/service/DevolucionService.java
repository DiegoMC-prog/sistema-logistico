package com.tienda.backend.devolucion.application.service;

import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import com.tienda.backend.devolucion.application.model.DevolucionVista;
import com.tienda.backend.devolucion.application.model.RegistrarCambioComando;
import com.tienda.backend.devolucion.application.model.RegistrarDevolucionComando;
import com.tienda.backend.devolucion.application.model.ResultadoRegistroDevolucion;
import com.tienda.backend.devolucion.application.port.in.ConsultarDevolucionUseCase;
import com.tienda.backend.devolucion.application.port.in.RegistrarCambioUseCase;
import com.tienda.backend.devolucion.application.port.in.RegistrarDevolucionUseCase;
import com.tienda.backend.devolucion.application.port.out.DevolucionRepositoryPort;
import com.tienda.backend.devolucion.domain.exception.DevolucionNotFoundException;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Casos de uso de devoluciones y cambios. A propósito NO es transaccional: registrar se ejecuta en
 * {@link DevolucionEjecutor} (una transacción con los bloqueos) y recién después de confirmarse se arma la respuesta.
 */
@Service
@RequiredArgsConstructor
public class DevolucionService implements RegistrarDevolucionUseCase, RegistrarCambioUseCase, ConsultarDevolucionUseCase {

    /** Permiso para ver las devoluciones de todos (debe coincidir con el de la tabla permisos). */
    private static final String PERMISO_VER_TODAS = "VENTA_VER";

    private final DevolucionEjecutor ejecutor;
    private final DevolucionVistaFactory vistaFactory;
    private final DevolucionRepositoryPort devolucionRepositoryPort;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    public ResultadoRegistroDevolucion registrar(RegistrarDevolucionComando comando) {
        return conReintento(comando.idempotencyKey(), () -> ejecutor.registrarDevolucion(comando));
    }

    @Override
    public ResultadoRegistroDevolucion registrarCambio(RegistrarCambioComando comando) {
        return conReintento(comando.idempotencyKey(), () -> ejecutor.registrarCambio(comando));
    }

    private ResultadoRegistroDevolucion conReintento(String clave, java.util.function.Supplier<DevolucionEjecutor.Registrada> operacion) {
        try {
            DevolucionEjecutor.Registrada r = operacion.get();
            return new ResultadoRegistroDevolucion(vistaFactory.construir(r.devolucion()), r.creada());
        } catch (DataIntegrityViolationException e) {
            // Dos peticiones con la misma clave a la vez: el índice único rechazó a la segunda; gana la que se guardó
            if (clave != null) {
                Optional<Devolucion> ganadora = devolucionRepositoryPort
                        .findByUsuarioIdAndIdempotencyKey(usuarioActualPort.id(), clave);
                if (ganadora.isPresent()) {
                    return new ResultadoRegistroDevolucion(vistaFactory.construir(ganadora.get()), false);
                }
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DevolucionVista obtener(Long id) {
        Devolucion d = devolucionRepositoryPort.findById(id).orElseThrow(() -> new DevolucionNotFoundException(id));
        if (!d.esDe(usuarioActualPort.id()) && !usuarioActualPort.tienePermiso(PERMISO_VER_TODAS)) {
            throw new AccesoDenegadoException("No puede ver una devolución registrada por otro usuario.");
        }
        return vistaFactory.construir(d);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Devolucion> listar(DevolucionFiltro filtro, int page, int size) {
        // Quien no puede ver todas solo ve las suyas, pida lo que pida
        DevolucionFiltro efectivo = usuarioActualPort.tienePermiso(PERMISO_VER_TODAS) ? filtro
                : new DevolucionFiltro(filtro.ventaId(), usuarioActualPort.id(), filtro.turnoId(), filtro.desde(), filtro.hasta());
        return devolucionRepositoryPort.findAll(efectivo, PageResult.safePage(page), PageResult.safeSize(size));
    }
}
