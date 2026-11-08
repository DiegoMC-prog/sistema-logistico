package com.tienda.backend.auth.infrastructure.security;

import java.security.Principal;

/**
 * Principal de la petición autenticada; se obtiene en un controlador con {@code @AuthenticationPrincipal}.
 * Implementa {@link Principal} para que {@code Authentication.getName()} devuelva el username (lo usa la auditoría).
 */
public record UsuarioAutenticado(Long id, String username) implements Principal {
    @Override
    public String getName() {
        return username;
    }
}
