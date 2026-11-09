package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.domain.model.Caja;

public interface CambiarEstadoCajaUseCase {
    Caja activar(Long id);
    Caja desactivar(Long id);
}
