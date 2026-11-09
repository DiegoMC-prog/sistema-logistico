package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

import java.math.BigDecimal;

public class AbonoExcedeDeudaException extends InvalidBusinessRuleException {
    public AbonoExcedeDeudaException(BigDecimal deuda) {
        super("El abono supera la deuda pendiente del cliente (" + deuda.toPlainString() + ").");
    }
}
