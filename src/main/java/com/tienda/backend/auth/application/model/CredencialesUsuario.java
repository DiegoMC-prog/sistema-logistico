package com.tienda.backend.auth.application.model;

/** Datos para verificar una contraseña. El hash no sale del módulo de autenticación. */
public record CredencialesUsuario(Long usuarioId, String passwordHash, boolean activo) {
}
