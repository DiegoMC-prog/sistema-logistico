package com.tienda.backend.shared.domain.exception;

// 401 - Credenciales o token inválidos
public abstract class AutenticacionException extends DomainException {
    protected AutenticacionException(String message) {
        super(message);
    }
}
