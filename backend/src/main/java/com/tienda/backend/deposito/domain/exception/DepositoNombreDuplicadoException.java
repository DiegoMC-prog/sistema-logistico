package com.tienda.backend.deposito.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class DepositoNombreDuplicadoException extends BusinessConflictException {
    public DepositoNombreDuplicadoException(String nombre) {
        super("Ya existe un depósito registrado con el nombre: " + nombre);
    }
}
