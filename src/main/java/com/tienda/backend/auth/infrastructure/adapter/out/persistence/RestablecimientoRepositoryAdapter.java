package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.port.out.RestablecimientoRepositoryPort;
import com.tienda.backend.auth.domain.model.TokenRestablecimiento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestablecimientoRepositoryAdapter implements RestablecimientoRepositoryPort {
    private final TokenRestablecimientoJpaRepository jpaRepository;

    @Override
    public TokenRestablecimiento save(TokenRestablecimiento token) {
        return aDominio(jpaRepository.save(TokenRestablecimientoEntity.builder()
                .id(token.getId())
                .usuarioId(token.getUsuarioId())
                .tokenHash(token.getTokenHash())
                .expiraAt(token.getExpiraAt())
                .usadoAt(token.getUsadoAt())
                .ip(token.getIp())
                .build()));
    }

    @Override
    public Optional<TokenRestablecimiento> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::aDominio);
    }

    @Override
    public void invalidarPendientes(Long usuarioId, LocalDateTime ahora) {
        jpaRepository.invalidarPendientes(usuarioId, ahora);
    }

    @Override
    public long contarSolicitudesDesde(Long usuarioId, LocalDateTime desde) {
        return jpaRepository.countByUsuarioIdAndCreatedAtAfter(usuarioId, desde);
    }

    @Override
    public boolean marcarUsado(Long tokenId, LocalDateTime ahora) {
        return jpaRepository.marcarUsado(tokenId, ahora) == 1;
    }

    private TokenRestablecimiento aDominio(TokenRestablecimientoEntity e) {
        return new TokenRestablecimiento(e.getId(), e.getUsuarioId(), e.getTokenHash(), e.getExpiraAt(), e.getUsadoAt(),
                e.getIp());
    }
}
