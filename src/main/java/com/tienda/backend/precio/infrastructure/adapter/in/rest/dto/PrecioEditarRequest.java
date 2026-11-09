package com.tienda.backend.precio.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** El tipo de cliente y la presentación no se editan: se elimina el precio y se crea otro. */
public record PrecioEditarRequest(
        @DecimalMin(value = "0", inclusive = false, message = "La cantidad minima debe ser mayor a cero")
        @Digits(integer = 10, fraction = 4, message = "La cantidad minima admite hasta 10 enteros y 4 decimales")
        BigDecimal cantidadMinima,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0", message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 4, message = "El precio admite hasta 10 enteros y 4 decimales")
        BigDecimal precio
) {
}
