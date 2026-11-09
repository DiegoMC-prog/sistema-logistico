package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.domain.model.TokenRestablecimiento;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RestablecimientoRepositoryPort {
    TokenRestablecimiento save(TokenRestablecimiento token);

    Optional<TokenRestablecimiento> findByTokenHash(String tokenHash);

    /** Invalida los enlaces todavía pendientes del usuario (se pidió uno nuevo, o ya se restableció la contraseña). */
    void invalidarPendientes(Long usuarioId, LocalDateTime ahora);

    /** Cuántos enlaces se le generaron al usuario desde {@code desde}, usados o no. */
    long contarSolicitudesDesde(Long usuarioId, LocalDateTime desde);

    /** Gasta el enlace solo si seguía sin usar; false si otra petición se adelantó (dos usos simultáneos). */
    boolean marcarUsado(Long tokenId, LocalDateTime ahora);
}
