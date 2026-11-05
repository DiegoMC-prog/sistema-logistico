package com.tienda.backend.shared.domain.exception;

// 409 - Conflicto (duplicados, estados incompatibles)
public abstract class BusinessConflictException extends DomainException {
    protected BusinessConflictException(String message) {
        super(message);
    }
}