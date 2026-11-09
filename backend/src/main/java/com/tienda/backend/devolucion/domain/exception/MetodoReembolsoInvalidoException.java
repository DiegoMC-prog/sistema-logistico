package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class MetodoReembolsoInvalidoException extends InvalidBusinessRuleException {
    public MetodoReembolsoInvalidoException() {
        super("El crédito de devolución no se puede elegir como método de reembolso.");
    }
}
