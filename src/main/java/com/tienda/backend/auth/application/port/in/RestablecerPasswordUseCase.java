package com.tienda.backend.auth.application.port.in;

public interface RestablecerPasswordUseCase {
    /**
     * Cambia la contraseña con el token del enlace. Lo gasta, invalida los demás enlaces pendientes y cierra todas las
     * sesiones abiertas del usuario.
     *
     * @throws com.tienda.backend.auth.domain.exception.TokenRestablecimientoInvalidoException si el enlace no sirve
     * @throws com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException        si la contraseña no cumple las reglas
     */
    void restablecer(String token, String passwordNueva);
}
