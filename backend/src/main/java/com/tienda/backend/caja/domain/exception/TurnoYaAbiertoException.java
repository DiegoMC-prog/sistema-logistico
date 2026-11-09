package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TurnoYaAbiertoException extends BusinessConflictException {
    public TurnoYaAbiertoException() {
        super("La caja ya tiene un turno abierto.");
    }
}
