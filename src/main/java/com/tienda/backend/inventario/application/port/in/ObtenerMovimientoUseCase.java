package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;

public interface ObtenerMovimientoUseCase {
    MovimientoDetallado obtener(Long id);
}
