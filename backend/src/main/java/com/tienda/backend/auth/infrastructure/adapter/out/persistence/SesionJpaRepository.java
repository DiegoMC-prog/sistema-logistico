package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SesionJpaRepository extends JpaRepository<SesionEntity, Long> {

    Optional<SesionEntity> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("update SesionEntity s set s.revocadaAt = :ahora where s.usuarioId = :usuarioId and s.revocadaAt is null")
    int revocarTodasDeUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") LocalDateTime ahora);
}
