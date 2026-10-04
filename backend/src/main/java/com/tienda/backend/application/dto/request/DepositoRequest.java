package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepositoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede tener mas de 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripcion no tener mas de 500 caracteres")
        String descripcion,

        @NotNull(message = "El estado no pude estar vacio")
        Boolean activo
) {
}
