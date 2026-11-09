package com.tienda.backend.pedido.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class PedidoNotFoundException extends ResourceNotFoundException {
    public PedidoNotFoundException(Long id) {
        super("El pedido con ID " + id + " no existe.");
    }
}
