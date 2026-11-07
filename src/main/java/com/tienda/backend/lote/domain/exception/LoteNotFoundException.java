package com.tienda.backend.lote.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class LoteNotFoundException extends ResourceNotFoundException {
    public LoteNotFoundException(Long id) {
        super("El lote con ID " + id + " no existe.");
    }
}
