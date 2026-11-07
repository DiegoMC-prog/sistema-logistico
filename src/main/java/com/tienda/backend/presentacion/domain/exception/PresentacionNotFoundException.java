package com.tienda.backend.presentacion.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class PresentacionNotFoundException extends ResourceNotFoundException {
    public PresentacionNotFoundException(Long id) {
        super("La presentación con ID " + id + " no existe.");
    }

    public PresentacionNotFoundException(String codigoBarras) {
        super("No existe una presentación con el código de barras: " + codigoBarras);
    }
}
