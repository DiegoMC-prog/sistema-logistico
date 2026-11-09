package com.tienda.backend.tipocliente.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class TipoClienteDatosInvalidosException extends InvalidBusinessRuleException {
    public TipoClienteDatosInvalidosException(String message) {
        super(message);
    }
}
