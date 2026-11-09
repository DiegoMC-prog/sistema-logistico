package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class PrecioDatosInvalidosException extends InvalidBusinessRuleException {
    public PrecioDatosInvalidosException(String message) {
        super(message);
    }
}
