package com.tienda.backend.rol.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class RolDatosInvalidosException extends InvalidBusinessRuleException {
    public RolDatosInvalidosException(String message) {
        super(message);
    }
}
