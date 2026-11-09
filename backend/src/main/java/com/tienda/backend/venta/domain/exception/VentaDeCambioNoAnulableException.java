package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class VentaDeCambioNoAnulableException extends BusinessConflictException {
    public VentaDeCambioNoAnulableException(String numero) {
        super("La venta " + numero + " es la venta de un cambio y no se puede anular; registre una devolución.");
    }
}
