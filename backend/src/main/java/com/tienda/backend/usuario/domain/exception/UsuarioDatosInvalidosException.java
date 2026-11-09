package com.tienda.backend.usuario.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class UsuarioDatosInvalidosException extends InvalidBusinessRuleException {
    public UsuarioDatosInvalidosException(String message) {
        super(message);
    }
}
