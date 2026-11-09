package com.tienda.backend.proveedor.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.proveedor.domain.model.Proveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank(message = "El nombre de la empresa es obligatorio")
        @Size(max = 100, message = "El nombre de la empresa no debe tener mas de 100 caracteres")
        String nombreEmpresa,

        @NotBlank(message = "El nombre del contacto es obligatorio")
        @Size(max = 150, message = "El nombre del contacto no debe tener mas de 150 caracteres")
        String nombreContacto,

        @Size(max = 30, message = "El telefono no debe tener mas de 30 caracteres")
        String telefono,

        @Email(message = "El email no es valido")
        @Size(max = 100, message = "El email no debe tener mas de 100 caracteres")
        String email
) {
    public Proveedor toDomain() {
        return new Proveedor(nombreEmpresa, nombreContacto, telefono, email);
    }
}
