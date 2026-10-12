package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El nombre de usuario o correo electrónico es obligatorio.")
        String usernameOrEmail,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
