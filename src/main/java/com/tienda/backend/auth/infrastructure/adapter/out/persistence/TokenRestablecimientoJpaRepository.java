package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRestablecimientoJpaRepository extends JpaRepository<TokenRestablecimientoEntity, Long> {

    Optional<TokenRestablecimientoEntity> findByTokenHash(String tokenHash);

    long countByUsuarioIdAndCreatedAtAfter(Long usuarioId, LocalDateTime desde);

    @Modifying
    @Query("update TokenRestablecimientoEntity t set t.usadoAt = :ahora where t.usuarioId = :usuarioId and t.usadoAt is null")
    int invalidarPendientes(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);

    /** Condicionado a que siga sin usar: de dos peticiones simultáneas con el mismo enlace, solo una actualiza una fila. */
    @Modifying
    @Query("update TokenRestablecimientoEntity t set t.usadoAt = :ahora where t.id = :id and t.usadoAt is null")
    int marcarUsado(@Param("id") Long id, @Param("ahora") LocalDateTime ahora);
}
