package com.tienda.backend.presentacion.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class PresentacionDatosInvalidosException extends InvalidBusinessRuleException {
    public PresentacionDatosInvalidosException(String message) {
        super(message);
    }
}
