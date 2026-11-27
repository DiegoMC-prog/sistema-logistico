package com.tienda.backend.catalogo.application.port.out;

import com.tienda.backend.catalogo.application.model.CatalogoFiltro;
import com.tienda.backend.catalogo.application.model.CatalogoProducto;
import com.tienda.backend.shared.application.model.PageResult;

public interface CatalogoRepositoryPort {
    /** Productos activos ordenados por nombre, con sus presentaciones y stock; los precios van en null. */
    PageResult<CatalogoProducto> listar(CatalogoFiltro filtro, int page, int size);
}
