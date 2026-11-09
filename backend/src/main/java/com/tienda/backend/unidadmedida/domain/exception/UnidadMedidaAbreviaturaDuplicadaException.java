package com.tienda.backend.unidadmedida.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UnidadMedidaAbreviaturaDuplicadaException extends BusinessConflictException {
    public UnidadMedidaAbreviaturaDuplicadaException(String abreviatura) {
        super("Ya existe una unidad de medida registrada con la abreviatura: " + abreviatura);
    }
}
