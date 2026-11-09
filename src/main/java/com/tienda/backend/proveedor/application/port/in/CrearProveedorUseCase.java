package com.tienda.backend.proveedor.application.port.in;

import com.tienda.backend.proveedor.domain.model.Proveedor;

public interface CrearProveedorUseCase {
    Proveedor create(Proveedor proveedor);
}
