package com.tienda.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank(message = "El nombre de la empresa es obligatorio y no puede estar vacío.")
        @Size(max = 100, message = "El nombre de la empresa no puede superar los 100 caracteres.")
        String nombreEmpresa,

        @Size(max = 250, message = "La dirección de la empresa no puede superar los 250 caracteres.")
        String direccionEmpresa,

        @Size(max = 20, message = "El teléfono de la empresa no puede superar los 20 caracteres.")
        String telefonoEmpresa,

        @Size(max = 100, message = "El nombre de contacto no puede superar los 100 caracteres.")
        String nombreContacto,

        @Size(max = 20, message = "El teléfono de contacto no puede superar los 20 caracteres.")
        String telefonoContacto
) {
}
