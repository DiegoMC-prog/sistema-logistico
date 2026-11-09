package com.tienda.backend.deposito.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class DepositoNotFoundException extends ResourceNotFoundException {
    public DepositoNotFoundException(Long id) {
        super("El depósito con ID " + id + " no existe.");
    }
}
