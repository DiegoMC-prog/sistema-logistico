package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.PrecioDetalle;

public interface ResolverPrecioUseCase {
    /** Precio vigente para vender esa cantidad a ese tipo de cliente (null = cliente sin tipo). */
    PrecioDetalle resolver(Long presentacionId, java.math.BigDecimal cantidad, Long tipoClienteId);
}
