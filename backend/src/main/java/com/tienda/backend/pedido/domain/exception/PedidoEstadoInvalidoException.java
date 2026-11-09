package com.tienda.backend.pedido.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PedidoEstadoInvalidoException extends BusinessConflictException {
    public PedidoEstadoInvalidoException(String message) {
        super(message);
    }
}
