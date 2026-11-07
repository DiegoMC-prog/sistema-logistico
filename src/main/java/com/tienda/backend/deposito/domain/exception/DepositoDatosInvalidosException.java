package com.tienda.backend.deposito.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class DepositoDatosInvalidosException extends InvalidBusinessRuleException {
    public DepositoDatosInvalidosException(String message) {
        super(message);
    }
}
