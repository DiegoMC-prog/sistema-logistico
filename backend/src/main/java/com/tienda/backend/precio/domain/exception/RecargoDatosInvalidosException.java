package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class RecargoDatosInvalidosException extends InvalidBusinessRuleException {
    public RecargoDatosInvalidosException(String message) {
        super(message);
    }
}
