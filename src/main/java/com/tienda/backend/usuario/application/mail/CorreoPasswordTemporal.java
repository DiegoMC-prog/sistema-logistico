package com.tienda.backend.usuario.application.mail;

import com.tienda.backend.shared.application.model.CorreoMensaje;

import java.util.Map;

/** Correo con la contraseña temporal de un usuario. Solo arma el mensaje; el diseño está en la plantilla HTML. */
public final class CorreoPasswordTemporal {
    private static final String ASUNTO = "Acceso al sistema: contraseña temporal";
    private static final String PLANTILLA = "correo/password-temporal";

    private CorreoPasswordTemporal() {
    }

    public static CorreoMensaje crear(String email, String nombre, String username, String passwordTemporal) {
        return new CorreoMensaje(email, ASUNTO, PLANTILLA, Map.of(
                "nombre", nombre,
                "username", username,
                "passwordTemporal", passwordTemporal));
    }
}
