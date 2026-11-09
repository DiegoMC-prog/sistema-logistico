package com.tienda.backend.proveedor.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProveedorJpaRepository
        extends JpaRepository<ProveedorEntity, Long>, JpaSpecificationExecutor<ProveedorEntity> {
    boolean existsByNombreEmpresa(String nombreEmpresa);
    boolean existsByNombreEmpresaAndIdNot(String nombreEmpresa, Long id);
}
