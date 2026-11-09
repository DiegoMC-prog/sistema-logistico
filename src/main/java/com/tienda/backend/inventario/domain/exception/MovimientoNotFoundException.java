package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class MovimientoNotFoundException extends ResourceNotFoundException {
    public MovimientoNotFoundException(Long id) {
        super("El movimiento de inventario con ID " + id + " no existe.");
    }
}
