package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AbrirTurnoRequest(
        @NotNull(message = "El monto de apertura es obligatorio")
        @DecimalMin(value = "0", message = "El monto de apertura no puede ser negativo")
        @Digits(integer = 10, fraction = 4, message = "El monto admite hasta 10 enteros y 4 decimales")
        BigDecimal montoApertura
) {
}
