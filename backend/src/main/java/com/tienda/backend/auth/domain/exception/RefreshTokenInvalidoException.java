package com.tienda.backend.auth.domain.exception;

import com.tienda.backend.shared.domain.exception.AutenticacionException;

public class RefreshTokenInvalidoException extends AutenticacionException {
    public RefreshTokenInvalidoException() {
        super("La sesión expiró o no es válida. Inicie sesión de nuevo.");
    }
}
