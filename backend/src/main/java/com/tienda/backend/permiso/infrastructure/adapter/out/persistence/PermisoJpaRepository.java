package com.tienda.backend.permiso.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoJpaRepository extends JpaRepository<PermisoEntity, Long> {
}
