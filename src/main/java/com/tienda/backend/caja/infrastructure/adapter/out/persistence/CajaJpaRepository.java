package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CajaJpaRepository extends JpaRepository<CajaEntity, Long>, JpaSpecificationExecutor<CajaEntity> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
