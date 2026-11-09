package com.tienda.backend.auth.application.model;

/** Datos para escribirle a un usuario. Sin contraseña ni roles. */
public record ContactoUsuario(Long usuarioId, String username, String nombre, String email, boolean activo) {
}
