package com.tienda.backend.permiso.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

import java.util.Collection;

public class PermisoNotFoundException extends ResourceNotFoundException {
    public PermisoNotFoundException(Collection<Long> ids) {
        super("Los permisos con ID " + ids + " no existen.");
    }
}
