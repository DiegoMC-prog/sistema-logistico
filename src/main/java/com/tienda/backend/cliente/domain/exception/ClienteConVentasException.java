package com.tienda.backend.cliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ClienteConVentasException extends BusinessConflictException {
    public ClienteConVentasException() {
        super("El cliente no se puede eliminar porque tiene ventas registradas. "
                + "Puede desactivarlo para que no se use en nuevas ventas.");
    }
}
