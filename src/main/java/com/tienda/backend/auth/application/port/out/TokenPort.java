package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.application.model.ClaimsAcceso;
import com.tienda.backend.auth.application.model.DatosAcceso;

import java.util.Optional;

public interface TokenPort {
    String emitirAcceso(DatosAcceso datos);

    /** Vacío si el token está mal formado, la firma no coincide o expiró. */
    Optional<ClaimsAcceso> leerAcceso(String accessToken);

    long segundosDeVidaAcceso();

    long diasDeVidaRefresh();

    /** Refresh token aleatorio en claro (solo se entrega al cliente; en BD va su hash). */
    String generarRefreshToken();

    String hashRefreshToken(String refreshToken);
}
