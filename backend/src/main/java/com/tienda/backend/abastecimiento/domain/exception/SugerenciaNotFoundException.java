package com.tienda.backend.abastecimiento.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class SugerenciaNotFoundException extends ResourceNotFoundException {
    public SugerenciaNotFoundException(Long id) {
        super("No existe la sugerencia de reorden con id " + id + ".");
    }
}
