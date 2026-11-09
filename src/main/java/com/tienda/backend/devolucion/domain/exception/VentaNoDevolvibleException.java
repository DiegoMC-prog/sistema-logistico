package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class VentaNoDevolvibleException extends BusinessConflictException {
    public VentaNoDevolvibleException(String numero) {
        super("La venta " + numero + " está anulada y no admite devoluciones.");
    }
}
