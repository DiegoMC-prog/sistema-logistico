package com.tienda.backend.producto.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        String descripcion,

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId,

        @NotNull(message = "El stock minimo es obligatorio")
        @PositiveOrZero(message = "El stock minimo no puede ser negativo")
        BigDecimal stockMinimo,

        @NotNull(message = "Debe indicar si el producto es perecedero")
        Boolean esPerecedero
) {
}
