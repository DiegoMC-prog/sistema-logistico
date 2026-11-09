package com.tienda.backend.metodopago.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class MetodoPagoDatosInvalidosException extends InvalidBusinessRuleException {
    public MetodoPagoDatosInvalidosException(String message) {
        super(message);
    }
}
