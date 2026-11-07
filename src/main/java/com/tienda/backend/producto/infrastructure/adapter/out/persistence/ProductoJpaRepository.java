package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long>, JpaSpecificationExecutor<ProductoEntity> {
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<ProductoEntity> findAll(Specification<ProductoEntity> spec, Pageable pageable);

    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
