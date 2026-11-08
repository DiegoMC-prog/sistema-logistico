package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.port.out.SesionRepositoryPort;
import com.tienda.backend.auth.domain.model.Sesion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SesionRepositoryAdapter implements SesionRepositoryPort {
    private final SesionJpaRepository sesionJpaRepository;

    @Override
    public Sesion save(Sesion sesion) {
        return toDomain(sesionJpaRepository.save(toEntity(sesion)));
    }

    @Override
    public Optional<Sesion> findByRefreshTokenHash(String refreshTokenHash) {
        return sesionJpaRepository.findByRefreshTokenHash(refreshTokenHash).map(this::toDomain);
    }

    @Override
    public void revocarTodasDeUsuario(Long usuarioId, LocalDateTime ahora) {
        sesionJpaRepository.revocarTodasDeUsuario(usuarioId, ahora);
    }

    private Sesion toDomain(SesionEntity e) {
        return new Sesion(e.getId(), e.getUsuarioId(), e.getRefreshTokenHash(), e.getExpiraAt(),
                e.getRevocadaAt(), e.getIp(), e.getUserAgent());
    }

    private SesionEntity toEntity(Sesion s) {
        return SesionEntity.builder()
                .id(s.getId())
                .usuarioId(s.getUsuarioId())
                .refreshTokenHash(s.getRefreshTokenHash())
                .expiraAt(s.getExpiraAt())
                .revocadaAt(s.getRevocadaAt())
                .ip(s.getIp())
                .userAgent(s.getUserAgent())
                .build();
    }
}
