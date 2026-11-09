package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarCajaUseCase {
    PageResult<Caja> listar(CajaFiltro filtro, int page, int size);
}
