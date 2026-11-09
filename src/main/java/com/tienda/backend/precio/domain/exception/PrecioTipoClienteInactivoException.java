package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PrecioTipoClienteInactivoException extends BusinessConflictException {
    public PrecioTipoClienteInactivoException(String nombre) {
        super("El tipo de cliente " + nombre + " está inactivo y no se le pueden asignar precios nuevos.");
    }
}
