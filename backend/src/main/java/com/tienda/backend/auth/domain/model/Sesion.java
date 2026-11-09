package com.tienda.backend.auth.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/** Un inicio de sesión (un dispositivo). Su refresh token se guarda solo como hash y se rota en cada renovación. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sesion {
    @EqualsAndHashCode.Include
    private final Long id;

    private final Long usuarioId;
    private final String ip;
    private final String userAgent;

    private String refreshTokenHash;
    private LocalDateTime expiraAt;
    private LocalDateTime revocadaAt;

    public Sesion(Long id, Long usuarioId, String refreshTokenHash, LocalDateTime expiraAt,
                  LocalDateTime revocadaAt, String ip, String userAgent) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.refreshTokenHash = refreshTokenHash;
        this.expiraAt = expiraAt;
        this.revocadaAt = revocadaAt;
        this.ip = ip;
        this.userAgent = userAgent;
    }

    public static Sesion nueva(Long usuarioId, String refreshTokenHash, LocalDateTime expiraAt,
                               String ip, String userAgent) {
        return new Sesion(null, usuarioId, refreshTokenHash, expiraAt, null, ip, userAgent);
    }

    public boolean estaVigente(LocalDateTime ahora) {
        return revocadaAt == null && expiraAt.isAfter(ahora);
    }

    public void revocar(LocalDateTime ahora) {
        if (revocadaAt == null) {
            this.revocadaAt = ahora;
        }
    }

    /** Rotación: el refresh token anterior deja de servir. */
    public void rotar(String nuevoHash, LocalDateTime nuevaExpiracion) {
        this.refreshTokenHash = nuevoHash;
        this.expiraAt = nuevaExpiracion;
    }
}
