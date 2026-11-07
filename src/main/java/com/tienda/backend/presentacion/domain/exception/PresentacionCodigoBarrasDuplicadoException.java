package com.tienda.backend.presentacion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class PresentacionCodigoBarrasDuplicadoException extends BusinessConflictException {
    public PresentacionCodigoBarrasDuplicadoException(String codigoBarras) {
        super("Ya existe una presentación registrada con el código de barras: " + codigoBarras);
    }
}
