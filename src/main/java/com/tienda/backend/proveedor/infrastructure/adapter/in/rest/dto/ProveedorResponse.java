package com.tienda.backend.proveedor.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.proveedor.domain.model.Proveedor;

public record ProveedorResponse(
        Long id,
        String nombreEmpresa,
        String nombreContacto,
        String telefono,
        String email,
        String estado
) {
    public static ProveedorResponse from(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getNombreEmpresa(), p.getNombreContacto(), p.getTelefono(),
                p.getEmail(), p.getEstado().name());
    }
}
