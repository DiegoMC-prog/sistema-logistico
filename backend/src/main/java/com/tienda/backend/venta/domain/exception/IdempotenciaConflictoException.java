package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class IdempotenciaConflictoException extends BusinessConflictException {
    public IdempotenciaConflictoException() {
        super("La clave de idempotencia ya se usó con una venta distinta. Use una clave nueva para una venta nueva.");
    }
}
