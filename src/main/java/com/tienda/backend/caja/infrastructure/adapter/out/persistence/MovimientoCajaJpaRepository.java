package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoCajaJpaRepository extends JpaRepository<MovimientoCajaEntity, Long> {
    Page<MovimientoCajaEntity> findByTurnoId(Long turnoId, Pageable pageable);

    /** Filas {tipo, suma} del turno: una por cada tipo que tenga movimientos. */
    @Query("select m.tipo, coalesce(sum(m.monto), 0) from MovimientoCajaEntity m where m.turnoId = :turnoId group by m.tipo")
    List<Object[]> totalesPorTipo(@Param("turnoId") Long turnoId);
}
