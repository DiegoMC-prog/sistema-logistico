package com.tienda.backend.unidadmedida.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class UnidadMedidaDatosInvalidosException extends InvalidBusinessRuleException {
    public UnidadMedidaDatosInvalidosException(String message) {
        super(message);
    }
}
