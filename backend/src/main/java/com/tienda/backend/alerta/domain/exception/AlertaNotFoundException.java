package com.tienda.backend.alerta.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class AlertaNotFoundException extends ResourceNotFoundException {
    public AlertaNotFoundException(Long id) {
        super("No existe la alerta con id " + id + ".");
    }
}
