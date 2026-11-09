package com.tienda.backend.venta.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ProductoInactivoVentaException extends BusinessConflictException {
    public ProductoInactivoVentaException(String nombre) {
        super("El producto " + nombre + " está inactivo y no se puede vender.");
    }
}
