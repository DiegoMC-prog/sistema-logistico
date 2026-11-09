package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaDetalleJpaRepository extends JpaRepository<VentaDetalleEntity, Long> {
    List<VentaDetalleEntity> findByVentaIdOrderByIdAsc(Long ventaId);
}
