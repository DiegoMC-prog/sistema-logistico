package com.tienda.backend.shared.application.port.out;

/** Quién está realizando la operación; los servicios lo usan para registrar autoría (p. ej. quién asignó un rol). */
public interface UsuarioActualPort {
    /** Username del usuario autenticado, o "SISTEMA" si no hay uno. */
    String nombre();

    /**
     * Id del usuario autenticado.
     *
     * @throws com.tienda.backend.shared.domain.exception.UsuarioNoIdentificadoException si la operacion no tiene un usuario identificado
     */
    Long id();

    /** true si el usuario autenticado tiene el permiso indicado. */
    boolean tienePermiso(String permiso);
}
