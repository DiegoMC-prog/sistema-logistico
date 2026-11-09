package com.tienda.backend.shared.domain.exception;

// 400 - Parámetro inválido o precondición rota
public abstract class InvalidBusinessRuleException extends DomainException {
    protected InvalidBusinessRuleException(String message) {
        super(message);
    }
}
