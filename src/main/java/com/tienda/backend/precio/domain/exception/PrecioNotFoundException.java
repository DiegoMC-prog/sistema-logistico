package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class PrecioNotFoundException extends ResourceNotFoundException {
    public PrecioNotFoundException(Long id) {
        super("El precio con ID " + id + " no existe.");
    }
}
