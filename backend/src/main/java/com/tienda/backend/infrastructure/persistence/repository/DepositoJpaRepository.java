package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositoJpaRepository extends JpaRepository<DepositoEntity, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Page<DepositoEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    long countByEstado(Boolean estado);
}
