package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

import java.math.BigDecimal;

public class PrecioNoDisponibleException extends ResourceNotFoundException {
    public PrecioNoDisponibleException(Long presentacionId, BigDecimal cantidad) {
        super("La presentación " + presentacionId + " no tiene un precio activo aplicable para la cantidad "
                + cantidad.stripTrailingZeros().toPlainString() + ".");
    }
}
