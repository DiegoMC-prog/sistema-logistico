package com.tienda.backend.rol.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class RolDeSistemaException extends BusinessConflictException {
    public RolDeSistemaException(String nombre) {
        super("El rol " + nombre + " es de sistema y no se puede modificar ni eliminar.");
    }
}
