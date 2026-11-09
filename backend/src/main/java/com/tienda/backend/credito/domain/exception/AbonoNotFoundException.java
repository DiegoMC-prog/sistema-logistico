package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class AbonoNotFoundException extends ResourceNotFoundException {
    public AbonoNotFoundException(Long id) {
        super("No existe el abono con id " + id + ".");
    }
}
