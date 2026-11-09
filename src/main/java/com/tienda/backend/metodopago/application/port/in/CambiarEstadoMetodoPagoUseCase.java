package com.tienda.backend.metodopago.application.port.in;

import com.tienda.backend.metodopago.domain.model.MetodoPago;

public interface CambiarEstadoMetodoPagoUseCase {
    MetodoPago activar(Long id);
    MetodoPago desactivar(Long id);
}
