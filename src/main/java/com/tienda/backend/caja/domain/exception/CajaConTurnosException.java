package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CajaConTurnosException extends BusinessConflictException {
    public CajaConTurnosException() {
        super("La caja no se puede eliminar porque tiene turnos registrados. Puede desactivarla.");
    }
}
