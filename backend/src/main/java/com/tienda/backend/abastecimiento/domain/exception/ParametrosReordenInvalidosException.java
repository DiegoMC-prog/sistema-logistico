package com.tienda.backend.abastecimiento.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ParametrosReordenInvalidosException extends InvalidBusinessRuleException {
    public ParametrosReordenInvalidosException(String message) {
        super(message);
    }
}
