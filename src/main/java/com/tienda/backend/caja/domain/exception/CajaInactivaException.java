package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CajaInactivaException extends BusinessConflictException {
    public CajaInactivaException(String nombre) {
        super("La caja " + nombre + " está inactiva y no se puede usar.");
    }
}
