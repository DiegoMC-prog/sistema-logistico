package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

import java.math.BigDecimal;

public class StockInsuficienteProductoException extends BusinessConflictException {
    public StockInsuficienteProductoException(String producto, BigDecimal disponible, BigDecimal solicitado) {
        super("Stock insuficiente de " + producto + ": disponible " + disponible.stripTrailingZeros().toPlainString()
                + ", solicitado " + solicitado.stripTrailingZeros().toPlainString() + " (unidad base).");
    }
}
