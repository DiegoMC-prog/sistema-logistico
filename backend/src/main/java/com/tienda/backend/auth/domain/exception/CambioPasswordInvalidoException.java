package com.tienda.backend.auth.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class CambioPasswordInvalidoException extends InvalidBusinessRuleException {
    public CambioPasswordInvalidoException(String message) {
        super(message);
    }
}
