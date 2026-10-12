package com.tienda.backend.application.dto.response;

import com.tienda.backend.domain.model.Rol;

public record AuthResponse(
        String token,
        String tipo,
        Long id,
        String username,
        String email,
        Rol rol,
        long expiresIn
) {
    public static AuthResponse of(String token, Long id, String username, String email, Rol rol, long expiresIn) {
        return new AuthResponse(token, "Bearer", id, username, email, rol, expiresIn);
    }
}
