package com.tienda.backend.metodopago.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class MetodoPagoDeSistemaException extends BusinessConflictException {
    public MetodoPagoDeSistemaException(String nombre) {
        super("El método de pago " + nombre + " es de sistema y no se puede renombrar, desactivar ni eliminar.");
    }
}
