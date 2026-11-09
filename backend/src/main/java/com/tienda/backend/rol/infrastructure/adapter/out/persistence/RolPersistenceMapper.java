package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import com.tienda.backend.rol.domain.model.Rol;
import org.springframework.stereotype.Component;

@Component
public class RolPersistenceMapper {
    public Rol toDomain(RolEntity entity) {
        if (entity == null) return null;
        return new Rol(entity.getId(), entity.getNombre());
    }

    public RolEntity toEntity(Rol domain) {
        if (domain == null) return null;
        return RolEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .build();
    }
}
