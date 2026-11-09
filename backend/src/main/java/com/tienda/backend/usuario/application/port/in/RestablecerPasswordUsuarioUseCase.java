package com.tienda.backend.usuario.application.port.in;

public interface RestablecerPasswordUsuarioUseCase {
    /** Genera una nueva contraseña temporal y la envía por correo al usuario. */
    void restablecerPassword(Long id);
}
