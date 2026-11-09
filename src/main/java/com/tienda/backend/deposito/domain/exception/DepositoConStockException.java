package com.tienda.backend.deposito.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class DepositoConStockException extends BusinessConflictException {
    public DepositoConStockException() {
        super("No se puede eliminar un depósito que todavía tiene stock. Trasládelo o ajústelo primero.");
    }
}
