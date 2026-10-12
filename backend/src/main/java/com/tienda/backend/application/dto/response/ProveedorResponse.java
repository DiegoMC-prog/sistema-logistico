package com.tienda.backend.application.dto.response;

public record ProveedorResponse(
        Long id,
        String nombreEmpresa,
        String direccionEmpresa,
        String telefonoEmpresa,
        String nombreContacto,
        String telefonoContacto,
        Boolean estado
) {
}
