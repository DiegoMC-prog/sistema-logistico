package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.AjusteComando;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AjusteRequest(
        @NotNull(message = "El deposito es obligatorio")
        Long depositoId,

        @NotNull(message = "El lote es obligatorio")
        Long loteId,

        @NotNull(message = "El stock real es obligatorio")
        @PositiveOrZero(message = "El stock real no puede ser negativo")
        @Digits(integer = 10, fraction = 4, message = "El stock real admite hasta 10 enteros y 4 decimales")
        BigDecimal stockReal,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
    public AjusteComando toComando() {
        return new AjusteComando(depositoId, loteId, stockReal, motivo);
    }
}
