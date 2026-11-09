package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CuentaConAbonosException extends BusinessConflictException {
    public CuentaConAbonosException() {
        super("La venta a crédito ya tiene abonos registrados y no se puede anular.");
    }
}
