package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.ProveedorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Long> {

    boolean existsByNombreEmpresaIgnoreCase(String nombreEmpresa);

    Page<ProveedorEntity> findByNombreEmpresaContainingIgnoreCase(String nombreEmpresa, Pageable pageable);

    long countByEstado(Boolean estado);
}
