package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.domain.model.Sesion;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SesionRepositoryPort {
    Sesion save(Sesion sesion);
    Optional<Sesion> findByRefreshTokenHash(String refreshTokenHash);
    /** Marca como revocadas todas las sesiones vigentes del usuario. */
    void revocarTodasDeUsuario(Long usuarioId, LocalDateTime ahora);
}
