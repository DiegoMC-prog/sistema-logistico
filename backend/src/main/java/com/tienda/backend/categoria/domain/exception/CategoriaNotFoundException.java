package com.tienda.backend.categoria.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class CategoriaNotFoundException extends ResourceNotFoundException {
    public CategoriaNotFoundException(Long id) {
        super("La categoría con ID " + id + " no existe.");
    }
}
