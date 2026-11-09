package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class MetodoPagoInactivoException extends BusinessConflictException {
    public MetodoPagoInactivoException(String nombre) {
        super("El método de pago " + nombre + " está inactivo.");
    }
}
