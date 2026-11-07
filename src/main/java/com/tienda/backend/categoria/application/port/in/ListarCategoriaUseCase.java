package com.tienda.backend.categoria.application.port.in;

import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.categoria.application.model.CategoriaFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarCategoriaUseCase {
    PageResult<Categoria> listar(CategoriaFiltro filtro, int page, int size);
}
