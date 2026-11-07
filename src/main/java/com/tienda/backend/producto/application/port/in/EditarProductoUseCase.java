package com.tienda.backend.producto.application.port.in;

import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.domain.model.Producto;

public interface EditarProductoUseCase {
    ProductoDetalle editar(Long id, Producto producto);
}
