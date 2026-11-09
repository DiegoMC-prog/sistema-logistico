package com.tienda.backend.proveedor.application.port.in;

import com.tienda.backend.proveedor.domain.model.Proveedor;

public interface ObtenerProveedorUseCase {
    Proveedor obtener(Long id);
}
