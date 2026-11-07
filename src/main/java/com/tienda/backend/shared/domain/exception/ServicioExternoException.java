package com.tienda.backend.shared.domain.exception;

// 502 - Falla de un servicio externo (correo, etc.) del que depende la operación
public abstract class ServicioExternoException extends DomainException {
    protected ServicioExternoException(String message) {
        super(message);
    }
}
