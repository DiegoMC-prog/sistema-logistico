package com.tienda.backend.domain.exception;

public class RecursoNoEncontrado extends RuntimeException {
    public RecursoNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
