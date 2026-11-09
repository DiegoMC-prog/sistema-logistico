package com.tienda.backend.proveedor.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class ProveedorConPedidosException extends BusinessConflictException {
    public ProveedorConPedidosException() {
        super("El proveedor no se puede eliminar porque tiene pedidos registrados. Puede desactivarlo.");
    }
}
