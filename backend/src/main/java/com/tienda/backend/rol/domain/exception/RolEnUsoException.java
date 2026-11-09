package com.tienda.backend.rol.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class RolEnUsoException extends BusinessConflictException {
    public RolEnUsoException(String nombre) {
        super("El rol " + nombre + " está asignado a usuarios y no se puede eliminar.");
    }
}
