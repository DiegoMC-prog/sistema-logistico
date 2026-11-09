package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TurnoCerradoException extends BusinessConflictException {
    public TurnoCerradoException(Long id) {
        super("El turno " + id + " ya está cerrado.");
    }
}
