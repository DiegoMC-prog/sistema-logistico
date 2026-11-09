package com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TipoClienteJpaRepository
        extends JpaRepository<TipoClienteEntity, Long>, JpaSpecificationExecutor<TipoClienteEntity> {
    Optional<TipoClienteEntity> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
