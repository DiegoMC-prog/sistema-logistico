package com.tienda.backend.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequest(
        @NotBlank(message = "El token es obligatorio")
        @Size(max = 200, message = "El token no es valido")
        String token,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 8, max = 72, message = "La contrasena nueva debe tener entre 8 y 72 caracteres")
        String passwordNueva
) {
}
