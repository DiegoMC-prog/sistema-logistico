package com.tienda.backend.deposito.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.deposito.domain.model.Deposito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepositoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        String descripcion
) {
    public Deposito toDomain() {
        return new Deposito(nombre, descripcion);
    }
}
