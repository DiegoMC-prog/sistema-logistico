package com.tienda.backend.shared.domain.exception;

// 403 - El usuario está autenticado pero la regla de negocio no le permite actuar sobre ese recurso
public class AccesoDenegadoException extends DomainException {
    public AccesoDenegadoException(String message) {
        super(message);
    }
}
