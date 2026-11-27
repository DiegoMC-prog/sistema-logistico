package com.tienda.backend.catalogo.infrastructure.adapter.out.precio;

import com.tienda.backend.catalogo.application.model.PrecioCatalogo;
import com.tienda.backend.catalogo.application.port.out.PrecioCatalogoPort;
import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.application.port.in.ResolverPrecioUseCase;
import com.tienda.backend.precio.domain.exception.PrecioNoDisponibleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/** Usa la misma resolución de precio que el carrito (jerarquía de precios y recargo por escasez). */
@Component
@RequiredArgsConstructor
public class PrecioCatalogoAdapter implements PrecioCatalogoPort {

    private final ResolverPrecioUseCase resolverPrecio;

    @Override
    public Optional<PrecioCatalogo> precioUnitario(Long presentacionId, Long tipoClienteId) {
        try {
            PrecioDetalle detalle = resolverPrecio.resolver(presentacionId, BigDecimal.ONE, tipoClienteId);
            return Optional.of(new PrecioCatalogo(detalle.precioFinal(),
                    detalle.recargo() == null ? null : detalle.recargo().porcentaje()));
        } catch (PrecioNoDisponibleException e) {
            return Optional.empty();
        }
    }
}
