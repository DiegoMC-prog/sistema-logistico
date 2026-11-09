package com.tienda.backend.rol.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.rol.domain.model.Rol;

public record RolResponse(
        Long id,
        String nombre
) {
    public static RolResponse from(Rol r) {
        return new RolResponse(r.getId(), r.getNombre());
    }
}
