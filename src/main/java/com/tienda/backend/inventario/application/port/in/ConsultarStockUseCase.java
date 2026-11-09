package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.StockDetalle;
import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarStockUseCase {
    PageResult<StockDetalle> consultarStock(StockFiltro filtro, int page, int size);
}
