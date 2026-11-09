package com.tienda.backend.lote.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class LoteNumeroDuplicadoException extends BusinessConflictException {
    public LoteNumeroDuplicadoException(String numeroLote) {
        super("Ya existe un lote con el número " + numeroLote + " para este producto.");
    }
}
