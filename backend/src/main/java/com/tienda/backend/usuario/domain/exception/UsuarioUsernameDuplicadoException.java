package com.tienda.backend.usuario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UsuarioUsernameDuplicadoException extends BusinessConflictException {
    public UsuarioUsernameDuplicadoException(String username) {
        super("Ya existe un usuario registrado con el username: " + username);
    }
}
