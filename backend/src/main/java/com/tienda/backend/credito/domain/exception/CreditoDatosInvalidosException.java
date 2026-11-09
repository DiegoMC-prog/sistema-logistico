package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class CreditoDatosInvalidosException extends InvalidBusinessRuleException {
    public CreditoDatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
