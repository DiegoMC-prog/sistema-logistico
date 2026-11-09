package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ClienteInactivoException extends BusinessConflictException {
    public ClienteInactivoException(String nombre) {
        super("El cliente " + nombre + " está inactivo y no se le puede vender.");
    }
}
