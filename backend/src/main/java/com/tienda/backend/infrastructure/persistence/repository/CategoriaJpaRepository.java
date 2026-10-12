package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoriaJpaRepository extends JpaRepository<CategoriaEntity, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Page<CategoriaEntity> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
