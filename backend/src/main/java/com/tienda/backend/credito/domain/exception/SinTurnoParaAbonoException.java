package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class SinTurnoParaAbonoException extends BusinessConflictException {
    public SinTurnoParaAbonoException() {
        super("Para registrar un abono necesita un turno de caja abierto.");
    }
}
