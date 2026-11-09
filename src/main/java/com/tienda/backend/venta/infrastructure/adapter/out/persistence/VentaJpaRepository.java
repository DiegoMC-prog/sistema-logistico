package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long>, JpaSpecificationExecutor<VentaEntity> {
    Optional<VentaEntity> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);

    /** {@code SELECT ... FOR UPDATE}: una segunda anulación simultánea espera y luego ve la venta ya anulada. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VentaEntity v where v.id = :id")
    Optional<VentaEntity> findParaActualizar(@Param("id") Long id);
}
