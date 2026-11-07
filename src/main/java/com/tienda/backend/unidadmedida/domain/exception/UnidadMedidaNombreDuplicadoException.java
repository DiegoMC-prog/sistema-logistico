package com.tienda.backend.unidadmedida.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UnidadMedidaNombreDuplicadoException extends BusinessConflictException {
    public UnidadMedidaNombreDuplicadoException(String nombre) {
        super("Ya existe una unidad de medida registrada con el nombre: " + nombre);
    }
}
