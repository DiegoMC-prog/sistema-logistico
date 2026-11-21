package com.tienda.backend.precio.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.precio.domain.model.AlcanceRecargo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Activar un recargo por escasez. Según el alcance se envía {@code productoId} o {@code categoriaId}. */
public record RecargoRequest(
        @NotNull(message = "Indique si el recargo es para un producto o para una categoría")
        AlcanceRecargo alcance,

        Long productoId,

        Long categoriaId,

        @NotNull(message = "Indique el porcentaje del recargo")
        @DecimalMin(value = "0.0001", message = "El recargo debe ser mayor que 0 %")
        @DecimalMax(value = "1", message = "El recargo es una fracción y no puede superar 1 (100 %)")
        BigDecimal porcentaje,

        @NotBlank(message = "Indique el motivo del recargo")
        @Size(max = 500, message = "El motivo admite hasta 500 caracteres")
        String motivo
) {
}
