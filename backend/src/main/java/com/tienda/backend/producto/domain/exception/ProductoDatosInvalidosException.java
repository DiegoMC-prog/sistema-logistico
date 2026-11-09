package com.tienda.backend.producto.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ProductoDatosInvalidosException extends InvalidBusinessRuleException {
    public ProductoDatosInvalidosException(String message) {
        super(message);
    }
}
