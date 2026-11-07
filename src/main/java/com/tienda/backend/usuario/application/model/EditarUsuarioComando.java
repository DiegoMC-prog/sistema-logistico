package com.tienda.backend.usuario.application.model;

/** Datos editables de un usuario; el username no se puede cambiar. */
public record EditarUsuarioComando(String nombre, String email) {
}
