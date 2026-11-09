package com.tienda.backend.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;

/**
 * Configuración del JWT ({@code app.seguridad.jwt.*}). La aplicación no arranca si el secreto falta o es demasiado corto:
 * una clave débil permitiría falsificar tokens.
 */
@ConfigurationProperties(prefix = "app.seguridad.jwt")
public record JwtProperties(String secret, int accessMinutos, int refreshDias) {
    private static final int SECRET_MIN_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < SECRET_MIN_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET es obligatorio y debe tener al menos " + SECRET_MIN_BYTES + " bytes. "
                            + "Genere uno con: python -c \"import secrets; print(secrets.token_urlsafe(48))\"");
        }
        if (accessMinutos < 1) {
            throw new IllegalStateException("JWT_ACCESS_MINUTOS debe ser al menos 1");
        }
        if (refreshDias < 1) {
            throw new IllegalStateException("JWT_REFRESH_DIAS debe ser al menos 1");
        }
    }
}
