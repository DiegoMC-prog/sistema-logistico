package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TurnoCerradoNoAnulableException extends BusinessConflictException {
    public TurnoCerradoNoAnulableException(String numero) {
        super("La venta " + numero + " pertenece a un turno de caja ya cerrado y no se puede anular; registre una devolución.");
    }
}
