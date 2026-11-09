package com.tienda.backend.presentacion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PresentacionBaseNoEliminableException extends BusinessConflictException {
    public PresentacionBaseNoEliminableException() {
        super("No se puede eliminar la presentación base mientras el producto tenga otras presentaciones.");
    }
}
