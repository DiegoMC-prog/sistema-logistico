package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.TrasladoComando;

public interface RegistrarTrasladoUseCase {
    MovimientoDetallado registrarTraslado(TrasladoComando comando);
}
