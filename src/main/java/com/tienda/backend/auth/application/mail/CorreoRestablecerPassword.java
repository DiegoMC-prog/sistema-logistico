package com.tienda.backend.auth.application.mail;

import com.tienda.backend.shared.application.model.CorreoMensaje;

import java.util.Map;

/** Correo con el enlace para restablecer la contraseña. Solo arma el mensaje; el diseño está en la plantilla HTML. */
public final class CorreoRestablecerPassword {
    private static final String ASUNTO = "Restablecer su contraseña";
    private static final String PLANTILLA = "correo/restablecer-password";

    private CorreoRestablecerPassword() {
    }

    public static CorreoMensaje crear(String email, String nombre, String enlace, int minutosValidez) {
        return new CorreoMensaje(email, ASUNTO, PLANTILLA, Map.of(
                "nombre", nombre,
                "enlace", enlace,
                "minutos", minutosValidez));
    }
}
