package com.tienda.backend.proveedor.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ProveedorInactivoException extends InvalidBusinessRuleException {
    public ProveedorInactivoException(Long id) {
        super("El proveedor con ID " + id + " está inactivo.");
    }
}
