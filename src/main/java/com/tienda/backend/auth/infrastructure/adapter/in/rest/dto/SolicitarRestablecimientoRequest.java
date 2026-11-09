package com.tienda.backend.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code identificador}: el nombre de usuario o el correo electrónico de la cuenta. */
public record SolicitarRestablecimientoRequest(
        @NotBlank(message = "Indique su usuario o correo")
        @Size(max = 255, message = "El usuario o correo no debe tener mas de 255 caracteres")
        String identificador
) {
}
