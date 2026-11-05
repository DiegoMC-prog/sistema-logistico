package com.tienda.backend.categoria.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class CategoriaNombreDuplicadoException extends BusinessConflictException {
    public CategoriaNombreDuplicadoException(String nombre) {
        super("Ya existe una categoría registrada con el nombre: " + nombre);
    }
}
