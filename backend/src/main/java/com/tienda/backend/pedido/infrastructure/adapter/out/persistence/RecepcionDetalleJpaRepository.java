package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RecepcionDetalleJpaRepository extends JpaRepository<RecepcionDetalleEntity, Long> {
    List<RecepcionDetalleEntity> findByRecepcionIdInOrderByIdAsc(Collection<Long> recepcionIds);
}
