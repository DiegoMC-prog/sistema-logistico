package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class VentaConDevolucionesException extends BusinessConflictException {
    public VentaConDevolucionesException(String numero) {
        super("La venta " + numero + " ya tiene devoluciones registradas y no se puede anular.");
    }
}
