package com.tienda.backend.metodopago.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class MetodoPagoNotFoundException extends ResourceNotFoundException {
    public MetodoPagoNotFoundException(Long id) {
        super("El método de pago con ID " + id + " no existe.");
    }
}
