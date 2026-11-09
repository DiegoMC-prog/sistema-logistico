package com.tienda.backend.tipocliente.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no debe tener mas de 100 caracteres")
        String nombre
) {
    public TipoCliente toDomain() {
        return new TipoCliente(nombre);
    }
}
