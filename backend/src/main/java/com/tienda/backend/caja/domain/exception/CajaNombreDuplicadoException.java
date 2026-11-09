package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CajaNombreDuplicadoException extends BusinessConflictException {
    public CajaNombreDuplicadoException(String nombre) {
        super("Ya existe una caja registrada con el nombre: " + nombre);
    }
}
