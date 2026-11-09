package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;

public interface CambiarEstadoPrecioUseCase {
    PrecioDetalle activar(Long id);
    PrecioDetalle desactivar(Long id);
}
