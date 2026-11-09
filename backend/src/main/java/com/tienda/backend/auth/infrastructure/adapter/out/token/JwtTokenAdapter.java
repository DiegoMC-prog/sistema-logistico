package com.tienda.backend.auth.infrastructure.adapter.out.token;

import com.tienda.backend.auth.application.model.ClaimsAcceso;
import com.tienda.backend.auth.application.model.DatosAcceso;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Access token JWT firmado con HS256. Lleva roles y permisos para no consultar la BD en cada petición, y
 * {@code ems} (emisión en milisegundos) porque {@code iat} solo tiene precisión de segundos y la revocación necesita más.
 */
@Component
public class JwtTokenAdapter implements TokenPort {
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISOS = "permisos";
    private static final String CLAIM_PASSWORD_TEMPORAL = "pt";
    private static final String CLAIM_EMISION_MS = "ems";

    private final JwtProperties propiedades;
    private final SecretKey clave;
    private final SecureRandom random = new SecureRandom();

    public JwtTokenAdapter(JwtProperties propiedades) {
        this.propiedades = propiedades;
        this.clave = Keys.hmacShaKeyFor(propiedades.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String emitirAcceso(DatosAcceso datos) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(datos.usuarioId()))
                .claim(CLAIM_USERNAME, datos.username())
                .claim(CLAIM_ROLES, datos.roles())
                .claim(CLAIM_PERMISOS, datos.permisos())
                .claim(CLAIM_PASSWORD_TEMPORAL, datos.passwordTemporal())
                .claim(CLAIM_EMISION_MS, ahora.toEpochMilli())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(Duration.ofMinutes(propiedades.accessMinutos()))))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public Optional<ClaimsAcceso> leerAcceso(String accessToken) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(accessToken).getPayload();

            return Optional.of(new ClaimsAcceso(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_USERNAME, String.class),
                    lista(claims.get(CLAIM_ROLES)),
                    lista(claims.get(CLAIM_PERMISOS)),
                    Boolean.TRUE.equals(claims.get(CLAIM_PASSWORD_TEMPORAL, Boolean.class)),
                    ((Number) claims.get(CLAIM_EMISION_MS)).longValue()));
        } catch (JwtException | IllegalArgumentException | ClassCastException | NullPointerException e) {
            // Firma inválida, expirado, mal formado o sin los claims esperados: se trata como token inválido.
            return Optional.empty();
        }
    }

    @Override
    public long segundosDeVidaAcceso() {
        return Duration.ofMinutes(propiedades.accessMinutos()).toSeconds();
    }

    @Override
    public long diasDeVidaRefresh() {
        return propiedades.refreshDias();
    }

    @Override
    public String generarRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hashRefreshToken(String refreshToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    @Override
    public String generarTokenOpaco() {
        return generarRefreshToken();
    }

    @Override
    public String hashTokenOpaco(String token) {
        return hashRefreshToken(token);
    }

    private static List<String> lista(Object valor) {
        if (valor instanceof List<?> elementos) {
            return elementos.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
