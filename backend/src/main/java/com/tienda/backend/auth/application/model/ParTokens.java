package com.tienda.backend.auth.application.model;

public record ParTokens(String accessToken, String refreshToken, long expiraEnSegundos, boolean passwordTemporal) {
}
