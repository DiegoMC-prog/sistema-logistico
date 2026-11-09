package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarMovimientosUseCase {
    PageResult<MovimientoDetallado> listar(MovimientoFiltro filtro, int page, int size);
}
