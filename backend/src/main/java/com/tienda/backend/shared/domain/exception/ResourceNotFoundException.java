package com.tienda.backend.shared.domain.exception;

// 404 - Recurso no encontrado
public abstract class ResourceNotFoundException extends DomainException {
    protected ResourceNotFoundException(String message) {
        super(message);
    }
}