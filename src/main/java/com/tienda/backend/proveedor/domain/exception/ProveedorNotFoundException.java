package com.tienda.backend.proveedor.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class ProveedorNotFoundException extends ResourceNotFoundException {
    public ProveedorNotFoundException(Long id) {
        super("El proveedor con ID " + id + " no existe.");
    }
}
