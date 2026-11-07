package com.tienda.backend.usuario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UsuarioEmailDuplicadoException extends BusinessConflictException {
    public UsuarioEmailDuplicadoException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
