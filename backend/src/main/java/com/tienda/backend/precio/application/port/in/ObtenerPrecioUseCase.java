package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;

public interface ObtenerPrecioUseCase {
    PrecioDetalle obtener(Long id);
}
