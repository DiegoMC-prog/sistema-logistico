package com.tienda.backend.shared.domain.exception;

/** Se superó el límite de solicitudes permitido para una operación sensible (se responde 429). */
public class DemasiadasSolicitudesException extends DomainException {
    public DemasiadasSolicitudesException(String message) {
        super(message);
    }
}
