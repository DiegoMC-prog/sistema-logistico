package com.tienda.backend.rol.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.permiso.infrastructure.adapter.in.rest.dto.PermisoResponse;
import com.tienda.backend.rol.application.model.RolDetalle;

import java.util.List;

public record RolDetalleResponse(
        Long id,
        String nombre,
        List<PermisoResponse> permisos
) {
    public static RolDetalleResponse from(RolDetalle d) {
        return new RolDetalleResponse(
                d.rol().getId(),
                d.rol().getNombre(),
                d.permisos().stream().map(PermisoResponse::from).toList());
    }
}
