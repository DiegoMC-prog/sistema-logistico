package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CajaConTurnoAbiertoException extends BusinessConflictException {
    public CajaConTurnoAbiertoException() {
        super("La caja tiene un turno abierto. Ciérrelo antes de modificarla, desactivarla o eliminarla.");
    }
}
