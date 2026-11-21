package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.DatosAcceso;
import com.tienda.backend.auth.application.port.in.VerificarCredencialesUseCase;
import com.tienda.backend.auth.application.port.out.BitacoraAutorizacionPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.CredencialesInvalidasException;
import com.tienda.backend.shared.application.model.AutorizacionOtorgada;
import com.tienda.backend.shared.application.model.CredencialesSupervisor;
import com.tienda.backend.shared.application.model.OperacionAutorizable;
import com.tienda.backend.shared.application.port.out.AutorizacionOperacionPort;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AutorizacionInvalidaException;
import com.tienda.backend.shared.domain.exception.AutorizacionRequeridaException;
import com.tienda.backend.shared.domain.exception.DemasiadasSolicitudesException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * R08 (fraude interno en caja): devoluciones, cambios y anulaciones por más de {@code app.autorizacion.monto-umbral}
 * exigen la autorización de un usuario con OPERACION_AUTORIZAR. Quien ya tiene ese permiso se autoriza a sí mismo;
 * los demás envían usuario y contraseña de un administrador, que cuentan para el bloqueo por intentos fallidos (R06).
 * Cada autorización queda en la bitácora autorizaciones_operacion.
 */
@Service
public class AutorizacionOperacionService implements AutorizacionOperacionPort {

    /** Debe coincidir con el permiso de la migración V11. */
    static final String PERMISO_AUTORIZAR = "OPERACION_AUTORIZAR";

    private final VerificarCredencialesUseCase verificarCredenciales;
    private final UsuarioAutenticacionPort usuarioAutenticacionPort;
    private final UsuarioActualPort usuarioActualPort;
    private final BitacoraAutorizacionPort bitacora;
    /** 0 o negativo = sin autorización por monto. */
    private final BigDecimal umbral;

    public AutorizacionOperacionService(VerificarCredencialesUseCase verificarCredenciales,
                                        UsuarioAutenticacionPort usuarioAutenticacionPort,
                                        UsuarioActualPort usuarioActualPort,
                                        BitacoraAutorizacionPort bitacora,
                                        @Value("${app.autorizacion.monto-umbral:100}") BigDecimal umbral) {
        this.verificarCredenciales = verificarCredenciales;
        this.usuarioAutenticacionPort = usuarioAutenticacionPort;
        this.usuarioActualPort = usuarioActualPort;
        this.bitacora = bitacora;
        this.umbral = umbral == null ? BigDecimal.ZERO : umbral.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Optional<AutorizacionOtorgada> exigir(OperacionAutorizable operacion, BigDecimal monto,
                                                 CredencialesSupervisor credenciales) {
        if (umbral.signum() <= 0 || monto == null || monto.compareTo(umbral) <= 0) {
            return Optional.empty();
        }
        BigDecimal valor = monto.setScale(2, RoundingMode.HALF_UP);
        String solicitante = usuarioActualPort.nombre();

        if (usuarioActualPort.tienePermiso(PERMISO_AUTORIZAR)) {
            return Optional.of(new AutorizacionOtorgada(operacion, valor, umbral, solicitante, solicitante));
        }
        if (credenciales == null || credenciales.vacias()) {
            throw new AutorizacionRequeridaException(operacion.descripcion(), valor, umbral);
        }

        Long supervisorId;
        try {
            supervisorId = verificarCredenciales.verificar(credenciales.username(), credenciales.password());
        } catch (CredencialesInvalidasException e) {
            throw new AutorizacionInvalidaException("Usuario o contraseña del administrador incorrectos.");
        } catch (DemasiadasSolicitudesException e) {
            throw new AutorizacionInvalidaException("La cuenta del administrador está bloqueada por intentos fallidos.");
        }
        DatosAcceso supervisor = usuarioAutenticacionPort.cargarDatosAcceso(supervisorId)
                .filter(DatosAcceso::activo)
                .orElseThrow(() -> new AutorizacionInvalidaException("Usuario o contraseña del administrador incorrectos."));
        if (!supervisor.permisos().contains(PERMISO_AUTORIZAR)) {
            throw new AutorizacionInvalidaException("El usuario " + supervisor.username()
                    + " no tiene permiso para autorizar operaciones.");
        }
        return Optional.of(new AutorizacionOtorgada(operacion, valor, umbral, solicitante, supervisor.username()));
    }

    @Override
    public void registrar(Optional<AutorizacionOtorgada> autorizacion, Long referenciaId) {
        autorizacion.ifPresent(a -> bitacora.registrar(a, referenciaId));
    }
}
