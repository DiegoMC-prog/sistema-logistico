package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.domain.model.Caja;

public interface ObtenerCajaUseCase {
    Caja obtener(Long id);
}
