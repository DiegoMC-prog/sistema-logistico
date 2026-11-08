package com.tienda.backend.auth.application.model;

import java.util.List;

/** Lo necesario para emitir un access token y para el perfil: identidad, estado y permisos efectivos (por todos sus roles). */
public record DatosAcceso(
        Long usuarioId,
        String username,
        String nombre,
        String email,
        boolean activo,
        boolean passwordTemporal,
        List<String> roles,
        List<String> permisos
) {
}
