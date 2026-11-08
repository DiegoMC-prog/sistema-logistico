package com.tienda.backend.auth.infrastructure.adapter.in.event;

import com.tienda.backend.auth.application.port.in.RevocarAccesoUseCase;
import com.tienda.backend.shared.application.event.AccesoDeRolModificado;
import com.tienda.backend.shared.application.event.AccesoUsuarioModificado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Atiende los cambios de acceso publicados por los módulos usuario y rol. Se ejecuta DESPUÉS de confirmarse el cambio:
 * si este se revierte, nadie pierde la sesión sin motivo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccesoModificadoListener {
    private final RevocarAccesoUseCase revocarAccesoUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alModificarseUsuario(AccesoUsuarioModificado evento) {
        try {
            revocarAccesoUseCase.revocarAccesoDeUsuario(evento.usuarioId());
        } catch (RuntimeException e) {
            log.error("No se pudo revocar el acceso del usuario {}", evento.usuarioId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alModificarseRol(AccesoDeRolModificado evento) {
        try {
            revocarAccesoUseCase.revocarAccesoDeRol(evento.rolId());
        } catch (RuntimeException e) {
            log.error("No se pudo revocar el acceso de los usuarios del rol {}", evento.rolId(), e);
        }
    }
}
