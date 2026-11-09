package com.tienda.backend.cliente.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ClienteDocumentoDuplicadoException extends BusinessConflictException {
    public ClienteDocumentoDuplicadoException(String documento) {
        super("Ya existe un cliente registrado con el documento: " + documento);
    }
}
