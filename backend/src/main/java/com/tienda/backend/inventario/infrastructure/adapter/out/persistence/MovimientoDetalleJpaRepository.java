package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MovimientoDetalleJpaRepository extends JpaRepository<MovimientoDetalleEntity, Long> {

    List<MovimientoDetalleEntity> findByMovimientoId(Long movimientoId);

    /** Las líneas de toda una página de movimientos en una sola consulta. */
    List<MovimientoDetalleEntity> findByMovimientoIdIn(Collection<Long> movimientoIds);
}
