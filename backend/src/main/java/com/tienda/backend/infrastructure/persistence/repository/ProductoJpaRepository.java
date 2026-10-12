package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"categoria"})
    Optional<ProductoEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"categoria"})
    Page<ProductoEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM ProductoEntity p WHERE " +
           "(CAST(:busqueda AS string) IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')) OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%'))) AND " +
           "(CAST(:categoriaId AS long) IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(CAST(:estado AS boolean) IS NULL OR p.estado = :estado)")
    Page<ProductoEntity> buscarPorFiltros(
            @Param("busqueda") String busqueda,
            @Param("categoriaId") Long categoriaId,
            @Param("estado") Boolean estado,
            Pageable pageable
    );

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("SELECT COUNT(p) FROM ProductoEntity p WHERE p.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT p.categoria.id, COUNT(p) FROM ProductoEntity p WHERE p.categoria.id IN :categoriaIds GROUP BY p.categoria.id")
    java.util.List<Object[]> countProductosByCategoriaIds(@Param("categoriaIds") java.util.List<Long> categoriaIds);

    @Query("SELECT COUNT(DISTINCT p.categoria.id) FROM ProductoEntity p")
    long countCategoriasEnUso();

    long countByEstado(Boolean estado);
}
