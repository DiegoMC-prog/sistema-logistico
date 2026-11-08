package com.tienda.backend.shared.infrastructure.config.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Nombre del usuario que realiza la operación en curso, para la auditoría. Fuera de una petición autenticada es "SISTEMA". */
public final class UsuarioActual {
    public static final String SISTEMA = "SISTEMA";

    private UsuarioActual() {
    }

    public static String nombre() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return SISTEMA;
        }
        String nombre = autenticacion.getName();
        return (nombre == null || nombre.isBlank()) ? SISTEMA : nombre;
    }
}
