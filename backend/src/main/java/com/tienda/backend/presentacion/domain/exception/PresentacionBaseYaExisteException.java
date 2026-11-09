package com.tienda.backend.presentacion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PresentacionBaseYaExisteException extends BusinessConflictException {
    public PresentacionBaseYaExisteException(Long productoId) {
        super("El producto con ID " + productoId + " ya tiene una presentación base.");
    }
}
