package com.tienda.backend.producto.application.port.in;

import com.tienda.backend.producto.application.model.ProductoDetalle;

public interface CambiarEstadoProductoUseCase {
    ProductoDetalle activar(Long id);
    ProductoDetalle desactivar(Long id);
}
