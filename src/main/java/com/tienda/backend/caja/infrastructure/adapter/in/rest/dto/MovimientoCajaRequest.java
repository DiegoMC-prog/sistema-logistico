package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoCajaRequest(
        @NotNull(message = "El tipo es obligatorio (INGRESO o EGRESO)")
        TipoMovimientoCaja tipo,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0", inclusive = false, message = "El monto debe ser mayor a cero")
        @Digits(integer = 10, fraction = 4, message = "El monto admite hasta 10 enteros y 4 decimales")
        BigDecimal monto,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no debe tener mas de 255 caracteres")
        String motivo
) {
}
