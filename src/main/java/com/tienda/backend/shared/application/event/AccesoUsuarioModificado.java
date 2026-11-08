package com.tienda.backend.shared.application.event;

/**
 * Cambió algo que invalida los tokens ya emitidos de un usuario (inactivado, eliminado, roles distintos,
 * contraseña restablecida). Lo publica el módulo dueño del cambio y lo atiende el módulo de autenticación.
 */
public record AccesoUsuarioModificado(Long usuarioId) {
}
