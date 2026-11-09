package com.tienda.backend.producto.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ProductoNombreDuplicadoException extends BusinessConflictException {
    public ProductoNombreDuplicadoException(String nombre) {
        super("Ya existe un producto registrado con el nombre: " + nombre);
    }
}
