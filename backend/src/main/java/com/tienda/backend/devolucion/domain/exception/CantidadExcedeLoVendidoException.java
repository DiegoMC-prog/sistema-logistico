package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CantidadExcedeLoVendidoException extends BusinessConflictException {
    public CantidadExcedeLoVendidoException(Long ventaDetalleId, java.math.BigDecimal maxima) {
        super("La línea " + ventaDetalleId + " solo admite devolver hasta " + maxima.stripTrailingZeros().toPlainString() + " más (ya devuelto lo demás).");
    }
}
