package com.tienda.backend.pedido.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class PedidoDatosInvalidosException extends InvalidBusinessRuleException {
    public PedidoDatosInvalidosException(String message) {
        super(message);
    }
}
