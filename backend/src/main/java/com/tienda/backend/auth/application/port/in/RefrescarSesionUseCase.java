package com.tienda.backend.auth.application.port.in;

import com.tienda.backend.auth.application.model.ParTokens;

public interface RefrescarSesionUseCase {
    /** Entrega un par de tokens nuevo (rotación): el refresh token enviado deja de servir. */
    ParTokens refrescar(String refreshToken);
}
