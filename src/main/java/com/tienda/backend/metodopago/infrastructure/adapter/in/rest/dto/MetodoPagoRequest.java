package com.tienda.backend.metodopago.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.metodopago.domain.model.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetodoPagoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no debe tener mas de 100 caracteres")
        String nombre
) {
    public MetodoPago toDomain() {
        return new MetodoPago(nombre);
    }
}
