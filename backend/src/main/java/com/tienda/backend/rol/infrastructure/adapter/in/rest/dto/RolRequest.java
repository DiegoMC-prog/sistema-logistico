package com.tienda.backend.rol.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.rol.domain.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre
) {
    public Rol toDomain() {
        return new Rol(nombre);
    }
}
