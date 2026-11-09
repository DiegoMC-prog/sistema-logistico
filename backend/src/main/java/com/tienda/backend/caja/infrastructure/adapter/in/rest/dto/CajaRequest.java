package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.domain.model.Caja;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CajaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no debe tener mas de 100 caracteres")
        String nombre,

        @NotNull(message = "El deposito es obligatorio")
        Long depositoId
) {
    public Caja toDomain() {
        return new Caja(nombre, depositoId);
    }
}
