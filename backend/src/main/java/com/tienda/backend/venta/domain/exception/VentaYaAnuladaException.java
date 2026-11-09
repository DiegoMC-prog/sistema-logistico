package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class VentaYaAnuladaException extends BusinessConflictException {
    public VentaYaAnuladaException(String numero) {
        super("La venta " + numero + " ya está anulada.");
    }
}
