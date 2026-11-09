package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class SinTurnoAbiertoException extends BusinessConflictException {
    public SinTurnoAbiertoException() {
        super("No tiene un turno de caja abierto. Abra un turno de caja para vender.");
    }
}
