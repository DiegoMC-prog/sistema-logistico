package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class VentaDatosInvalidosException extends InvalidBusinessRuleException {
    public VentaDatosInvalidosException(String message) {
        super(message);
    }
}
