package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class TurnoNotFoundException extends ResourceNotFoundException {
    public TurnoNotFoundException(Long id) {
        super("El turno de caja con ID " + id + " no existe.");
    }
}
