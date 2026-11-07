package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresentacionJpaRepository extends JpaRepository<PresentacionEntity, Long> {

    @EntityGraph(attributePaths = "unidadMedida")
    List<PresentacionEntity> findByProductoIdOrderByEsUnidadBaseDescIdAsc(Long productoId);

    @EntityGraph(attributePaths = "unidadMedida")
    Optional<PresentacionEntity> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    boolean existsByProductoIdAndEsUnidadBaseTrue(Long productoId);

    long countByProductoId(Long productoId);
}
