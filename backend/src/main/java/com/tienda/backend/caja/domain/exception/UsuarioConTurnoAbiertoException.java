package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UsuarioConTurnoAbiertoException extends BusinessConflictException {
    public UsuarioConTurnoAbiertoException() {
        super("El usuario ya tiene un turno abierto. Ciérrelo antes de abrir otro.");
    }
}
