package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class SinTurnoAbiertoException extends BusinessConflictException {
    public SinTurnoAbiertoException() {
        super("No tiene un turno de caja abierto. Abra un turno para registrar devoluciones: el reembolso sale de su caja.");
    }
}
