package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, Long> {
    List<RolPermisoEntity> findByRolId(Long rolId);
}
