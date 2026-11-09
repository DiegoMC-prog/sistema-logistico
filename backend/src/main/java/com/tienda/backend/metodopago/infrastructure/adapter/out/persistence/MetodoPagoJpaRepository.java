package com.tienda.backend.metodopago.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MetodoPagoJpaRepository
        extends JpaRepository<MetodoPagoEntity, Long>, JpaSpecificationExecutor<MetodoPagoEntity> {
    Optional<MetodoPagoEntity> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
