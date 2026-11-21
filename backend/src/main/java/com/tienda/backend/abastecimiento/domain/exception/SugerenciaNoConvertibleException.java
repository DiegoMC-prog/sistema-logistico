package com.tienda.backend.abastecimiento.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

/** La sugerencia ya se convirtió en pedido, se descartó o no tiene proveedor. */
public class SugerenciaNoConvertibleException extends BusinessConflictException {
    public SugerenciaNoConvertibleException(String message) {
        super(message);
    }
}
