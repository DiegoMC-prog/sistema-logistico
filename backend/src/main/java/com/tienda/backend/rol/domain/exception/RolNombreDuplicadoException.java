package com.tienda.backend.rol.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class RolNombreDuplicadoException extends BusinessConflictException {
    public RolNombreDuplicadoException(String nombre) {
        super("Ya existe un rol registrado con el nombre: " + nombre);
    }
}
