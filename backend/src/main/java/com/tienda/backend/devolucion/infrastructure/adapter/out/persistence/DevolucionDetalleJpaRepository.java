package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DevolucionDetalleJpaRepository extends JpaRepository<DevolucionDetalleEntity, Long> {
    List<DevolucionDetalleEntity> findByDevolucionIdOrderByIdAsc(Long devolucionId);

    /** Filas {ventaDetalleId, loteId, suma} de lo ya devuelto de una venta. */
    @Query("""
            select d.ventaDetalleId, d.loteId, sum(d.cantidad)
            from DevolucionDetalleEntity d, DevolucionEntity v
            where v.id = d.devolucionId and v.ventaId = :ventaId
            group by d.ventaDetalleId, d.loteId
            """)
    List<Object[]> devueltoPorLinea(@Param("ventaId") Long ventaId);
}
