package com.tienda.backend.permiso.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.permiso.domain.model.Permiso;

public record PermisoResponse(
        Long id,
        String nombre,
        String descripcion
) {
    public static PermisoResponse from(Permiso p) {
        return new PermisoResponse(p.getId(), p.getNombre(), p.getDescripcion());
    }
}
