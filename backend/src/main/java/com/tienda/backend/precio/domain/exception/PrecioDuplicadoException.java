package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PrecioDuplicadoException extends BusinessConflictException {
    public PrecioDuplicadoException() {
        super("Ya existe un precio para esa presentación, ese tipo de cliente y esa cantidad mínima.");
    }
}
