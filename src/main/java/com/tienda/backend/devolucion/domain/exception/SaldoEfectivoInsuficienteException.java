package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class SaldoEfectivoInsuficienteException extends BusinessConflictException {
    public SaldoEfectivoInsuficienteException(java.math.BigDecimal disponible) {
        super("No hay efectivo suficiente en la caja para reembolsar (disponible " + disponible.toPlainString() + "). Reembolse con otro método de pago.");
    }
}
