package com.tienda.backend.tipocliente.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class TipoClienteNotFoundException extends ResourceNotFoundException {
    public TipoClienteNotFoundException(Long id) {
        super("El tipo de cliente con ID " + id + " no existe.");
    }
}
