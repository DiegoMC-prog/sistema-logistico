package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class MovimientoInvalidoException extends InvalidBusinessRuleException {
    public MovimientoInvalidoException(String message) {
        super(message);
    }
}
