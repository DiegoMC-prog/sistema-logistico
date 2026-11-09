package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class PagoInsuficienteException extends InvalidBusinessRuleException {
    public PagoInsuficienteException(java.math.BigDecimal faltante) {
        super("Los pagos no cubren el total de la venta: faltan " + faltante.toPlainString() + ".");
    }
}
