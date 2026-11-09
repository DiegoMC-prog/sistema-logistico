package com.tienda.backend.auth.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

/** Enlace de un solo uso para restablecer la contraseña. Del token en claro solo se conserva su hash. */
@Getter
public class TokenRestablecimiento {
    private final Long id;
    private final Long usuarioId;
    private final String tokenHash;
    private final LocalDateTime expiraAt;
    private final LocalDateTime usadoAt;
    private final String ip;

    public TokenRestablecimiento(Long id, Long usuarioId, String tokenHash, LocalDateTime expiraAt,
                                 LocalDateTime usadoAt, String ip) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraAt = expiraAt;
        this.usadoAt = usadoAt;
        this.ip = ip;
    }

    public static TokenRestablecimiento nuevo(Long usuarioId, String tokenHash, LocalDateTime expiraAt, String ip) {
        return new TokenRestablecimiento(null, usuarioId, tokenHash, expiraAt, null, ip);
    }

    /** Vigente = todavía no se usó ni se invalidó, y no pasó su fecha de vencimiento. */
    public boolean estaVigente(LocalDateTime ahora) {
        return usadoAt == null && ahora.isBefore(expiraAt);
    }
}
