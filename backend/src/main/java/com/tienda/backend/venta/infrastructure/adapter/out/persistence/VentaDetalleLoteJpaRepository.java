package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VentaDetalleLoteJpaRepository extends JpaRepository<VentaDetalleLoteEntity, Long> {
    List<VentaDetalleLoteEntity> findByVentaDetalleIdInOrderByIdAsc(Collection<Long> ventaDetalleIds);
}
