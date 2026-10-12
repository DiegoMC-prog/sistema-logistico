package com.tienda.backend.application.dto.response;

import com.tienda.backend.domain.model.Rol;

public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombre,
        String apellido,
        Rol rol,
        Boolean estado
) {
}
