package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class IdempotenciaConflictoException extends BusinessConflictException {
    public IdempotenciaConflictoException() {
        super("La clave de idempotencia ya se usó con una devolución distinta. Use una clave nueva para una devolución nueva.");
    }
}
