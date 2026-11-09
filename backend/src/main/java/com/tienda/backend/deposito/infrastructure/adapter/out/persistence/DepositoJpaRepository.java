package com.tienda.backend.deposito.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepositoJpaRepository extends JpaRepository<DepositoEntity, Long>, JpaSpecificationExecutor<DepositoEntity> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
