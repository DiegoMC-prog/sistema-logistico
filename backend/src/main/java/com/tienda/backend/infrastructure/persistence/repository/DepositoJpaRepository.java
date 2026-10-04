package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositoJpaRepository extends JpaRepository<DepositoEntity, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByDescripcionIgnoreCase(String descripcion);
}
