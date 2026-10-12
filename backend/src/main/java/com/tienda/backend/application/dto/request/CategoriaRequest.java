package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio y no puede estar vacío.")
        @Size(max = 100, message = "El nombre de la categoría no puede superar los 100 caracteres.")
        String nombre
) {
}
