package com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnidadMedidaJpaRepository
        extends JpaRepository<UnidadMedidaEntity, Long>, JpaSpecificationExecutor<UnidadMedidaEntity> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    boolean existsByAbreviatura(String abreviatura);
    boolean existsByAbreviaturaAndIdNot(String abreviatura, Long id);
}
