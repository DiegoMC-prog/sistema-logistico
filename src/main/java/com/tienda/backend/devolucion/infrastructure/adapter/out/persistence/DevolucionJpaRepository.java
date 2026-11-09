package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DevolucionJpaRepository extends JpaRepository<DevolucionEntity, Long>, JpaSpecificationExecutor<DevolucionEntity> {
    Optional<DevolucionEntity> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);
}
