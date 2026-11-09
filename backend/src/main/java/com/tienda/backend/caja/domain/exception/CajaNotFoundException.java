package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class CajaNotFoundException extends ResourceNotFoundException {
    public CajaNotFoundException(Long id) {
        super("La caja con ID " + id + " no existe.");
    }
}
