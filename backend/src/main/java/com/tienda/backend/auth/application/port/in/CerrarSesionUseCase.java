package com.tienda.backend.auth.application.port.in;

public interface CerrarSesionUseCase {
    /** Cierra la sesión de ese refresh token. Es idempotente: si no existe, no hace nada. */
    void cerrarSesion(String refreshToken);

    /** Cierra todas las sesiones del usuario e invalida sus access tokens vigentes. */
    void cerrarTodas(Long usuarioId);
}
