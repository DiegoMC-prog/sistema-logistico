package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.AjusteComando;
import com.tienda.backend.inventario.application.model.MovimientoDetallado;

public interface RegistrarAjusteUseCase {
    MovimientoDetallado registrarAjuste(AjusteComando comando);
}
