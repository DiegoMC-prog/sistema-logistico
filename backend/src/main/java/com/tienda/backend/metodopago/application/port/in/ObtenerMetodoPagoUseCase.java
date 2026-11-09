package com.tienda.backend.metodopago.application.port.in;

import com.tienda.backend.metodopago.domain.model.MetodoPago;

public interface ObtenerMetodoPagoUseCase {
    MetodoPago obtener(Long id);
}
