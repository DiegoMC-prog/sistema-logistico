package com.tienda.backend.usuario.application.model;

/** Datos personales para crear un usuario; la contraseña la genera el sistema y se envía por correo. */
public record CrearUsuarioComando(String username, String nombre, String email) {
}
