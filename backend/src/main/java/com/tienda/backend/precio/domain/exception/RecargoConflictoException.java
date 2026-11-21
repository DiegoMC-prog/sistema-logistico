package com.tienda.backend.precio.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

/** Ya hay un recargo activo para ese destino, o el recargo ya estaba desactivado. */
public class RecargoConflictoException extends BusinessConflictException {
    public RecargoConflictoException(String message) {
        super(message);
    }
}
