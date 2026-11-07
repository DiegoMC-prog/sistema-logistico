package com.tienda.backend.producto.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class ProductoNotFoundException extends ResourceNotFoundException {
    public ProductoNotFoundException(Long id) {
        super("El producto con ID " + id + " no existe.");
    }
}
