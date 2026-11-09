package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.port.out.PrecioVentaPort;
import com.tienda.backend.precio.application.port.in.ResolverPrecioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class VentaPrecioAdapter implements PrecioVentaPort {
    private final ResolverPrecioUseCase resolverPrecioUseCase;

    @Override
    public BigDecimal precioAplicable(Long presentacionId, BigDecimal cantidad, Long tipoClienteId) {
        return resolverPrecioUseCase.resolver(presentacionId, cantidad, tipoClienteId).precio().getPrecio();
    }
}
