package com.tienda.backend.lote.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class LoteDatosInvalidosException extends InvalidBusinessRuleException {
    public LoteDatosInvalidosException(String message) {
        super(message);
    }
}
