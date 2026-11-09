package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class DevolucionNotFoundException extends ResourceNotFoundException {
    public DevolucionNotFoundException(Long id) {
        super("La devolución con ID " + id + " no existe.");
    }
}
