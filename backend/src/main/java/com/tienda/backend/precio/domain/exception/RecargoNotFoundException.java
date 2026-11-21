package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class RecargoNotFoundException extends ResourceNotFoundException {
    public RecargoNotFoundException(Long id) {
        super("No existe el recargo por escasez con id " + id + ".");
    }
}
