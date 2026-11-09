package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class SaldoCajaInsuficienteException extends BusinessConflictException {
    public SaldoCajaInsuficienteException(java.math.BigDecimal disponible) {
        super("El egreso supera el efectivo disponible en caja (" + disponible.toPlainString() + ").");
    }
}
