package com.tienda.backend.categoria.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class CategoriaNombreInvalidoException extends InvalidBusinessRuleException {
    public CategoriaNombreInvalidoException() {
        super("El nombre es obligatorio y no puede estar vacío");
    }
}
