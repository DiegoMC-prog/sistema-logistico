package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepositoRequest(
        @NotBlank(message = "El nombre del depósito es obligatorio y no puede estar vacío.")
        @Size(max = 100, message = "El nombre del depósito no puede superar los 100 caracteres.")
        String nombre,

        @Size(max = 200, message = "La descripción del depósito no puede superar los 200 caracteres.")
        String descripcion
) {
}
