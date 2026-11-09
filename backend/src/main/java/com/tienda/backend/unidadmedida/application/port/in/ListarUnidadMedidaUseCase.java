package com.tienda.backend.unidadmedida.application.port.in;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

public interface ListarUnidadMedidaUseCase {
    PageResult<UnidadMedida> listar(UnidadMedidaFiltro filtro, int page, int size);
}
