package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class DevolucionDatosInvalidosException extends InvalidBusinessRuleException {
    public DevolucionDatosInvalidosException(String message) {
        super(message);
    }
}
