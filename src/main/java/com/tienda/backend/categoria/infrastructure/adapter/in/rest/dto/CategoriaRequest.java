package com.tienda.backend.categoria.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.categoria.domain.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre
) {
    public Categoria toDomain() {
        return new Categoria(nombre);
    }
}
