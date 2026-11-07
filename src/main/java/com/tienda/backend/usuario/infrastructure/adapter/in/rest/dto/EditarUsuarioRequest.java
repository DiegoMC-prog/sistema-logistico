package com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.usuario.application.model.EditarUsuarioComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 255, message = "El email no debe tener mas de 255 caracteres")
        String email
) {
    public EditarUsuarioComando toComando() {
        return new EditarUsuarioComando(nombre, email);
    }
}
