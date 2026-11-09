package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;

public interface EditarPrecioUseCase {
    PrecioDetalle editar(Long id, java.math.BigDecimal cantidadMinima, java.math.BigDecimal precio);
}
