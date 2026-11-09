package com.tienda.backend.cliente.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class ClienteNotFoundException extends ResourceNotFoundException {
    public ClienteNotFoundException(Long id) {
        super("El cliente con ID " + id + " no existe.");
    }
}
