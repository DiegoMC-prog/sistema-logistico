package com.tienda.backend.metodopago.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class MetodoPagoNombreDuplicadoException extends BusinessConflictException {
    public MetodoPagoNombreDuplicadoException(String nombre) {
        super("Ya existe un método de pago registrado con el nombre: " + nombre);
    }
}
