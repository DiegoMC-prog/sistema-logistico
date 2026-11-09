package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CajaDepositoInactivoException extends BusinessConflictException {
    public CajaDepositoInactivoException(String nombre) {
        super("El depósito " + nombre + " está inactivo y no se le pueden asignar cajas.");
    }
}
