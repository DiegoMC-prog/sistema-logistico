package com.tienda.backend.auth.infrastructure.adapter.out.notificacion;

import com.tienda.backend.auth.application.mail.CorreoPasswordCambiada;
import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.auth.application.port.out.NotificadorCambioPasswordPort;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Envía el aviso de cambio de contraseña en segundo plano. Si el envío falla solo se registra (sin el correo del
 * destinatario): el cambio de contraseña ya ocurrió y no debe depender del servidor de correo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificadorCambioPasswordAdapter implements NotificadorCambioPasswordPort {
    private final EnviadorCorreoPort enviadorCorreoPort;

    @Override
    @Async
    public void avisarCambio(ContactoUsuario destinatario, OrigenCambioPassword origen) {
        try {
            enviadorCorreoPort.enviar(CorreoPasswordCambiada.crear(destinatario.email(), destinatario.nombre(),
                    destinatario.username(), LocalDateTime.now(), origen));
        } catch (RuntimeException e) {
            log.warn("No se pudo enviar el aviso de cambio de contraseña al usuario {}: {}", destinatario.usuarioId(),
                    e.getMessage());
        }
    }
}
