package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.ItemEntradaComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Usar un lote existente ({@code loteId}) o crear uno nuevo ({@code lote}, junto con {@code productoId}). */
public record ItemEntradaRequest(
        Long productoId,

        Long loteId,

        @Valid
        NuevoLoteRequest lote,

        Long presentacionId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
        BigDecimal cantidad
) {
    public ItemEntradaComando toComando() {
        return new ItemEntradaComando(productoId, loteId, lote == null ? null : lote.toComando(),
                presentacionId, cantidad);
    }
}
