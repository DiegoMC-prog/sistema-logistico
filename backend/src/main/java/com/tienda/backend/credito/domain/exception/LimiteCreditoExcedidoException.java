package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

import java.math.BigDecimal;

public class LimiteCreditoExcedidoException extends InvalidBusinessRuleException {
    public LimiteCreditoExcedidoException(String cliente, BigDecimal disponible) {
        super("La venta a crédito supera el límite de " + cliente + ": crédito disponible " + disponible.toPlainString() + ".");
    }
}
