package com.tienda.backend.proveedor.application.port.in;

import com.tienda.backend.proveedor.domain.model.Proveedor;

public interface EditarProveedorUseCase {
    Proveedor editar(Long id, Proveedor proveedor);
}
