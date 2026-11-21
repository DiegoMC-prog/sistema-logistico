package com.tienda.backend.abastecimiento.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Un campo null vuelve al valor global. */
public record ParametrosReordenRequest(
        @PositiveOrZero(message = "El costo de emitir un pedido no puede ser negativo")
        BigDecimal costoPedido,

        @DecimalMin(value = "0.0001", message = "El costo de almacenamiento debe ser mayor que cero")
        @DecimalMax(value = "10", message = "El costo de almacenamiento es una fracción anual (0.25 = 25 %)")
        BigDecimal costoAlmacenPct,

        @DecimalMin(value = "0.5", message = "El nivel de servicio mínimo es 0.50")
        @DecimalMax(value = "0.999", message = "El nivel de servicio máximo es 0.999")
        BigDecimal nivelServicio,

        Long proveedorId,

        Boolean activo
) {
}
