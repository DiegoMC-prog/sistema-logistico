package com.tienda.backend.rol.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

import java.util.Collection;

public class RolNotFoundException extends ResourceNotFoundException {
    public RolNotFoundException(Long id) {
        super("El rol con ID " + id + " no existe.");
    }

    public RolNotFoundException(Collection<Long> ids) {
        super("Los roles con ID " + ids + " no existen.");
    }
}
