package com.tienda.backend.auth.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.auth.application.model.ParTokens;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean passwordTemporal
) {
    public static TokenResponse from(ParTokens tokens) {
        return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), "Bearer",
                tokens.expiraEnSegundos(), tokens.passwordTemporal());
    }
}
