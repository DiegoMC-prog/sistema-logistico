package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

import java.math.BigDecimal;

public class StockInsuficienteException extends BusinessConflictException {
    public StockInsuficienteException(Long loteId, BigDecimal disponible, BigDecimal solicitado) {
        super("Stock insuficiente en el lote " + loteId + ": disponible " + disponible.stripTrailingZeros().toPlainString()
                + ", solicitado " + solicitado.stripTrailingZeros().toPlainString() + " (unidad base).");
    }
}
