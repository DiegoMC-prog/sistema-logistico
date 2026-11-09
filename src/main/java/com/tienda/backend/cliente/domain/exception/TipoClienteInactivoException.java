package com.tienda.backend.cliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TipoClienteInactivoException extends BusinessConflictException {
    public TipoClienteInactivoException(String nombre) {
        super("El tipo de cliente " + nombre + " está inactivo y no se puede asignar a un cliente.");
    }
}
