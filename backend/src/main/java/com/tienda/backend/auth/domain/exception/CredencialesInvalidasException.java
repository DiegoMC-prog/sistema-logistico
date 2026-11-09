package com.tienda.backend.auth.domain.exception;

import com.tienda.backend.shared.domain.exception.AutenticacionException;

/** Mensaje único para cualquier fallo de login: no revela si el usuario existe, está inactivo o la clave es incorrecta. */
public class CredencialesInvalidasException extends AutenticacionException {
    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
