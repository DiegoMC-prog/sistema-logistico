package com.tienda.backend.unidadmedida.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class UnidadMedidaNotFoundException extends ResourceNotFoundException {
    public UnidadMedidaNotFoundException(Long id) {
        super("La unidad de medida con ID " + id + " no existe.");
    }
}
