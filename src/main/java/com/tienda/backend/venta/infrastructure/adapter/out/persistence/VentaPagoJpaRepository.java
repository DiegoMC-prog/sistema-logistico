package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaPagoJpaRepository extends JpaRepository<VentaPagoEntity, Long> {
    List<VentaPagoEntity> findByVentaIdOrderByIdAsc(Long ventaId);
}
