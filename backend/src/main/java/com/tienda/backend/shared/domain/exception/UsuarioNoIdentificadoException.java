package com.tienda.backend.shared.domain.exception;

public class UsuarioNoIdentificadoException extends AutenticacionException {
    public UsuarioNoIdentificadoException() {
        super("La operación requiere un usuario autenticado.");
    }
}
