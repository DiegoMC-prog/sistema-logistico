package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ProductoInactivoException extends BusinessConflictException {
    public ProductoInactivoException(Long productoId) {
        super("El producto con ID " + productoId + " está inactivo y no puede recibir stock.");
    }
}
