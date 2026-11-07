package com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AsignarRolesRequest(
        @NotNull(message = "La lista de roles es obligatoria (puede ir vacia)")
        Set<Long> rolIds
) {
}
