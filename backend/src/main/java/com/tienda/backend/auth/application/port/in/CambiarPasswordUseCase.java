package com.tienda.backend.auth.application.port.in;

public interface CambiarPasswordUseCase {
    /** Cambia la contraseña propia; cierra todas las sesiones y obliga a iniciar sesión de nuevo. */
    void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva);
}
