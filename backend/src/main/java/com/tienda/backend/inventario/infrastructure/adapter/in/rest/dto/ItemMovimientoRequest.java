package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.ItemMovimientoComando;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemMovimientoRequest(
        @NotNull(message = "El lote es obligatorio")
        Long loteId,

        Long presentacionId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
        BigDecimal cantidad
) {
    public ItemMovimientoComando toComando() {
        return new ItemMovimientoComando(loteId, presentacionId, cantidad);
    }
}
