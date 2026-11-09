package com.tienda.backend.unidadmedida.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadMedidaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        @NotBlank(message = "La abreviatura es obligatoria")
        @Size(max = 10, message = "La abreviatura no debe tener mas de 10 caracteres")
        String abreviatura
) {
    public UnidadMedida toDomain() {
        return new UnidadMedida(nombre, abreviatura);
    }
}
