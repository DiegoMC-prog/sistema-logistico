package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.SalidaComando;

public interface RegistrarSalidaUseCase {
    MovimientoDetallado registrarSalida(SalidaComando comando);
}
