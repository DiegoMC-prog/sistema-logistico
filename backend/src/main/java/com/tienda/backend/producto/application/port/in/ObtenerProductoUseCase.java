package com.tienda.backend.producto.application.port.in;

import com.tienda.backend.producto.application.model.ProductoDetalle;

public interface ObtenerProductoUseCase {
    ProductoDetalle obtener(Long id);
}
