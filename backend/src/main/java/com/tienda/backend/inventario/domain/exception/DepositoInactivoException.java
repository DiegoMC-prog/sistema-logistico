package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class DepositoInactivoException extends BusinessConflictException {
    public DepositoInactivoException(String nombre) {
        super("El depósito " + nombre + " está inactivo y no puede recibir stock.");
    }
}
