package com.tienda.backend.auth.infrastructure.adapter.out.notificacion;

import com.tienda.backend.auth.application.mail.CorreoRestablecerPassword;
import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.port.out.NotificadorRestablecimientoPort;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Envía el enlace por correo en segundo plano. La dirección del frontend ({@code app.frontend.url-restablecer},
 * variable FRONTEND_URL_RESTABLECER) sale de la configuración, no del código. Si el envío falla solo se registra:
 * quien pidió el enlace recibe siempre la misma respuesta. No se registra el token ni el correo del destinatario.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificadorRestablecimientoAdapter implements NotificadorRestablecimientoPort {
    private final EnviadorCorreoPort enviadorCorreoPort;

    @Value("${app.frontend.url-restablecer}")
    private String urlRestablecer;

    @Override
    @Async
    public void enviarEnlace(ContactoUsuario destinatario, String token, int minutosValidez) {
        try {
            enviadorCorreoPort.enviar(CorreoRestablecerPassword.crear(destinatario.email(), destinatario.nombre(),
                    enlace(token), minutosValidez));
        } catch (RuntimeException e) {
            log.warn("No se pudo enviar el enlace de restablecimiento al usuario {}: {}", destinatario.usuarioId(),
                    e.getMessage());
        }
    }

    /** {@code <url>?token=<token>}; el token es base64 URL-safe, no necesita codificación. */
    String enlace(String token) {
        return urlRestablecer + (urlRestablecer.contains("?") ? "&" : "?") + "token=" + token;
    }
}
