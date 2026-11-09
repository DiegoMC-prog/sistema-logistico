package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class VueltoSinEfectivoException extends InvalidBusinessRuleException {
    public VueltoSinEfectivoException() {
        super("Los pagos superan el total y el vuelto solo puede darse sobre un pago en efectivo mayor al sobrante.");
    }
}
