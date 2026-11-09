package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovimientoJpaRepository
        extends JpaRepository<MovimientoEntity, Long>, JpaSpecificationExecutor<MovimientoEntity> {
}
