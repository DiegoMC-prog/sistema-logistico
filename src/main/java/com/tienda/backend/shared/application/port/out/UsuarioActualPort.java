package com.tienda.backend.shared.application.port.out;

/** Quién está realizando la operación; los servicios lo usan para registrar autoría (p. ej. quién asignó un rol). */
public interface UsuarioActualPort {
    /** Username del usuario autenticado, o "SISTEMA" si no hay uno. */
    String nombre();
}
