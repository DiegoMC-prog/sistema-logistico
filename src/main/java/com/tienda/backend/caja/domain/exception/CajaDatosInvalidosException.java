package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class CajaDatosInvalidosException extends InvalidBusinessRuleException {
    public CajaDatosInvalidosException(String message) {
        super(message);
    }
}
