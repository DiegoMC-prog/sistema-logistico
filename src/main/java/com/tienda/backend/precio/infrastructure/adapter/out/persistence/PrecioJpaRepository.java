package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PrecioJpaRepository extends JpaRepository<PrecioEntity, Long>, JpaSpecificationExecutor<PrecioEntity> {

    // Con tipoClienteId nulo Spring Data compara con IS NULL (precio general)
    boolean existsByPresentacionIdAndTipoClienteIdAndCantidadMinima(Long presentacionId, Long tipoClienteId,
                                                                    BigDecimal cantidadMinima);

    boolean existsByPresentacionIdAndTipoClienteIdAndCantidadMinimaAndIdNot(Long presentacionId, Long tipoClienteId,
                                                                           BigDecimal cantidadMinima, Long id);

    @Query("""
            SELECT p FROM PrecioEntity p
            WHERE p.presentacionId = :presentacionId
              AND p.estado = 'ACTIVO'
              AND p.cantidadMinima <= :cantidad
              AND (p.tipoClienteId IS NULL OR p.tipoClienteId = :tipoClienteId)
            """)
    List<PrecioEntity> findCandidatos(@Param("presentacionId") Long presentacionId,
                                      @Param("cantidad") BigDecimal cantidad,
                                      @Param("tipoClienteId") Long tipoClienteId);
}
