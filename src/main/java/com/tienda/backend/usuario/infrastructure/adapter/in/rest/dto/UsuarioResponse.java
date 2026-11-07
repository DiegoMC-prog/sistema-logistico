package com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.usuario.application.model.UsuarioDetalle;
import com.tienda.backend.usuario.domain.model.Usuario;

import java.util.List;

/** Nunca incluye la contraseña ni su hash; solo indica si todavía es la temporal. */
public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String email,
        String estado,
        boolean passwordTemporal,
        List<RolResumen> roles
) {
    public record RolResumen(Long id, String nombre) {
    }

    public static UsuarioResponse from(UsuarioDetalle detalle) {
        Usuario u = detalle.usuario();
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getEmail(),
                u.getEstado().name(),
                u.isPasswordTemporal(),
                detalle.roles().stream().map(r -> new RolResumen(r.getId(), r.getNombre())).toList());
    }
}
