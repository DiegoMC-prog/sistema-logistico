package com.tienda.backend.catalogo.application.port.in;

import com.tienda.backend.catalogo.application.model.CatalogoFiltro;
import com.tienda.backend.catalogo.application.model.CatalogoProducto;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarCatalogoUseCase {
    PageResult<CatalogoProducto> consultar(CatalogoFiltro filtro, int page, int size);
}
