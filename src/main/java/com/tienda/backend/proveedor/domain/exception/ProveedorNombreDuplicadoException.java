package com.tienda.backend.proveedor.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ProveedorNombreDuplicadoException extends BusinessConflictException {
    public ProveedorNombreDuplicadoException(String nombreEmpresa) {
        super("Ya existe un proveedor registrado con el nombre de empresa: " + nombreEmpresa);
    }
}
