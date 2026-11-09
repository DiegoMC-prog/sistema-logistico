package com.tienda.backend.proveedor.application.port.in;

import com.tienda.backend.proveedor.domain.model.Proveedor;

public interface CambiarEstadoProveedorUseCase {
    Proveedor activar(Long id);
    Proveedor desactivar(Long id);
}
