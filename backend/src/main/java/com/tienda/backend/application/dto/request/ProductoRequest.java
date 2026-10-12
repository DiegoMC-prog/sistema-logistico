package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El código de barras es obligatorio y no puede estar vacío.")
        @Size(max = 100, message = "El código de barras no puede superar los 100 caracteres.")
        String codigoBarras,

        @NotBlank(message = "El nombre del producto es obligatorio y no puede estar vacío.")
        @Size(max = 200, message = "El nombre del producto no puede superar los 200 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
        String descripcion,

        String imagen,

        @NotNull(message = "El ID de la categoría es obligatorio.")
        @Positive(message = "El ID de la categoría debe ser mayor a 0.")
        Long categoriaId
) {
}
