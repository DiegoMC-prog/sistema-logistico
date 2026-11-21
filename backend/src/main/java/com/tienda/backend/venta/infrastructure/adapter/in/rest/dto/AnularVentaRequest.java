package com.tienda.backend.venta.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.shared.infrastructure.adapter.in.rest.dto.AutorizacionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnularVentaRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no debe tener mas de 255 caracteres")
        String motivo,

        /** R08: credenciales de un administrador si el total supera el monto umbral. */
        @Valid AutorizacionRequest autorizacion
) {
}
