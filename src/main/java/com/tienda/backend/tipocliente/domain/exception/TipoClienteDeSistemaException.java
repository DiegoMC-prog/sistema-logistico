package com.tienda.backend.tipocliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TipoClienteDeSistemaException extends BusinessConflictException {
    public TipoClienteDeSistemaException(String nombre) {
        super("El tipo de cliente " + nombre + " es de sistema y no se puede renombrar, desactivar ni eliminar.");
    }
}
