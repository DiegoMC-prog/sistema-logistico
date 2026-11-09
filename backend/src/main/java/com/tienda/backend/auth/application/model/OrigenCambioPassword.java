package com.tienda.backend.auth.application.model;

/** Cómo se cambió la contraseña; el correo de aviso lo cuenta con otras palabras. */
public enum OrigenCambioPassword {
    /** El usuario eligió una nueva con el enlace de "olvidé mi contraseña". */
    RESTABLECIMIENTO,
    /** El usuario la cambió desde su cuenta (incluye el cambio obligatorio de una contraseña temporal). */
    CAMBIO_PROPIO
}
