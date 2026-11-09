package com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.usuario.application.model.CrearUsuarioComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solo datos personales: la contraseña la genera el sistema y llega al usuario por correo. */
public record UsuarioRequest(
        @NotBlank(message = "El username es obligatorio")
        @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "El username solo puede contener letras, numeros, punto, guion y guion bajo")
        String username,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 255, message = "El email no debe tener mas de 255 caracteres")
        String email
) {
    public CrearUsuarioComando toComando() {
        return new CrearUsuarioComando(username, nombre, email);
    }
}
