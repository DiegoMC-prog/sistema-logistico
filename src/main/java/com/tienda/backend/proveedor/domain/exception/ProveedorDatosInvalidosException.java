package com.tienda.backend.proveedor.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ProveedorDatosInvalidosException extends InvalidBusinessRuleException {
    public ProveedorDatosInvalidosException(String message) {
        super(message);
    }
}
