package com.tienda.backend.cliente.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ClienteDatosInvalidosException extends InvalidBusinessRuleException {
    public ClienteDatosInvalidosException(String message) {
        super(message);
    }
}
