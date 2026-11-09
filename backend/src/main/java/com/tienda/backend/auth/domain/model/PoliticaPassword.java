package com.tienda.backend.auth.domain.model;

import com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException;

import java.nio.charset.StandardCharsets;

/** Reglas que cumple toda contraseña nueva, venga de un cambio normal o de un restablecimiento. */
public final class PoliticaPassword {
    public static final int MINIMO = 8;
    /** Límite de BCrypt: ignora lo que exceda de 72 bytes. */
    public static final int MAXIMO_BYTES = 72;

    private PoliticaPassword() {
    }

    /** @throws CambioPasswordInvalidoException si la contraseña no cumple las reglas */
    public static void validar(String password) {
        if (password == null || password.isBlank()) {
            throw new CambioPasswordInvalidoException("La nueva contraseña es obligatoria");
        }
        if (password.length() < MINIMO) {
            throw new CambioPasswordInvalidoException("La nueva contraseña debe tener al menos " + MINIMO + " caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            throw new CambioPasswordInvalidoException("La nueva contraseña no puede superar los " + MAXIMO_BYTES + " bytes");
        }
    }
}
