package com.tienda.backend.usuario.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class UsuarioNotFoundException extends ResourceNotFoundException {
    public UsuarioNotFoundException(Long id) {
        super("El usuario con ID " + id + " no existe.");
    }
}
