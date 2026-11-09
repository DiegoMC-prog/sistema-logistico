package com.tienda.backend.auth.application.mail;

import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.shared.application.model.CorreoMensaje;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Aviso de que la contraseña cambió. A propósito no lleva ningún enlace: un correo de seguridad con botones es el
 * formato que usan los engaños de phishing; solo indica qué hacer si el cambio no fue del usuario.
 */
public final class CorreoPasswordCambiada {
    private static final String ASUNTO = "Su contraseña fue cambiada";
    private static final String PLANTILLA = "correo/password-cambiada";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private CorreoPasswordCambiada() {
    }

    public static CorreoMensaje crear(String email, String nombre, String username, LocalDateTime cuando,
                                      OrigenCambioPassword origen) {
        return new CorreoMensaje(email, ASUNTO, PLANTILLA, Map.of(
                "nombre", nombre,
                "username", username,
                "fecha", cuando.format(FECHA),
                "origen", origen.name()));
    }
}
