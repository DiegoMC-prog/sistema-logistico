package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoteJpaRepository extends JpaRepository<LoteEntity, Long>, JpaSpecificationExecutor<LoteEntity> {

    @Override
    @EntityGraph(attributePaths = "producto")
    Page<LoteEntity> findAll(Specification<LoteEntity> spec, Pageable pageable);

    boolean existsByProductoIdAndNumeroLote(Long productoId, String numeroLote);

    boolean existsByProductoIdAndNumeroLoteAndIdNot(Long productoId, String numeroLote, Long id);
}
