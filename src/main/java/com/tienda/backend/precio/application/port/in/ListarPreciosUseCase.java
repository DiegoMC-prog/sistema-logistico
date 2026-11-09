package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.application.model.PrecioFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarPreciosUseCase {
    PageResult<PrecioDetalle> listar(Long presentacionId, PrecioFiltro filtro, int page, int size);
}
