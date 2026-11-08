package com.tienda.backend.auth.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.auth.application.model.DatosAcceso;

import java.util.List;

public record PerfilResponse(
        Long id,
        String username,
        String nombre,
        String email,
        boolean passwordTemporal,
        List<String> roles,
        List<String> permisos
) {
    public static PerfilResponse from(DatosAcceso datos) {
        return new PerfilResponse(datos.usuarioId(), datos.username(), datos.nombre(), datos.email(),
                datos.passwordTemporal(), datos.roles(), datos.permisos());
    }
}
