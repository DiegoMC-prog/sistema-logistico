package com.tienda.backend.producto.application.port.in;

import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarProductoUseCase {
    PageResult<ProductoDetalle> listar(ProductoFiltro filtro, int page, int size);
}
