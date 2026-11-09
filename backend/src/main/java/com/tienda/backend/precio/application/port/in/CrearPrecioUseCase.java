package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.domain.model.Precio;

public interface CrearPrecioUseCase {
    PrecioDetalle create(Long presentacionId, Precio precio);
}
