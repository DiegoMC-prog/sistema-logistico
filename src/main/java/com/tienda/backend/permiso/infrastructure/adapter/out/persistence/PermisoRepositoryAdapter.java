package com.tienda.backend.permiso.infrastructure.adapter.out.persistence;

import com.tienda.backend.permiso.application.port.out.PermisoRepositoryPort;
import com.tienda.backend.permiso.domain.model.Permiso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PermisoRepositoryAdapter implements PermisoRepositoryPort {
    private final PermisoJpaRepository permisoJpaRepository;

    @Override
    public List<Permiso> findAll() {
        return permisoJpaRepository.findAll(Sort.by("id")).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Permiso> findAllById(Collection<Long> ids) {
        return permisoJpaRepository.findAllById(ids).stream().map(this::toDomain).toList();
    }

    private Permiso toDomain(PermisoEntity entity) {
        return new Permiso(entity.getId(), entity.getNombre(), entity.getDescripcion());
    }
}
