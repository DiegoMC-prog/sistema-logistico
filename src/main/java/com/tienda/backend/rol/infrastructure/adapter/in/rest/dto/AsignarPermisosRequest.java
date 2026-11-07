package com.tienda.backend.rol.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AsignarPermisosRequest(
        @NotNull(message = "La lista de permisos es obligatoria (puede ir vacia)")
        Set<Long> permisoIds
) {
}
