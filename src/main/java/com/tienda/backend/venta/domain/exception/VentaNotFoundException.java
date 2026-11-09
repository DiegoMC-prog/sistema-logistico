package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class VentaNotFoundException extends ResourceNotFoundException {
    public VentaNotFoundException(Long id) {
        super("La venta con ID " + id + " no existe.");
    }
}
