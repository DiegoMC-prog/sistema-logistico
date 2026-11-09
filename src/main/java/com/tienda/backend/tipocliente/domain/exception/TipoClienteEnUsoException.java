package com.tienda.backend.tipocliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class TipoClienteEnUsoException extends BusinessConflictException {
    public TipoClienteEnUsoException() {
        super("El tipo de cliente no se puede eliminar porque tiene clientes, precios o ventas asociados. Puede desactivarlo.");
    }
}
