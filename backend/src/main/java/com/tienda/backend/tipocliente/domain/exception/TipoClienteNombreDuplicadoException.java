package com.tienda.backend.tipocliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TipoClienteNombreDuplicadoException extends BusinessConflictException {
    public TipoClienteNombreDuplicadoException(String nombre) {
        super("Ya existe un tipo de cliente registrado con el nombre: " + nombre);
    }
}
