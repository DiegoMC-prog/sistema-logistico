package com.tienda.backend.credito.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AbonoRequest(
        @NotNull(message = "El método de pago es obligatorio")
        Long metodoPagoId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        @Size(max = 100, message = "La referencia no debe tener mas de 100 caracteres")
        String referencia
) {
}
