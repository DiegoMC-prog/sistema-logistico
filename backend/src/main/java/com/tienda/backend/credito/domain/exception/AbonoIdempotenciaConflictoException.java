package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

/** R03: la clave de idempotencia ya se usó con un abono de otro contenido. */
public class AbonoIdempotenciaConflictoException extends BusinessConflictException {
    public AbonoIdempotenciaConflictoException() {
        super("La clave de idempotencia ya se usó con un abono distinto. Use una clave nueva para un abono nuevo.");
    }
}
