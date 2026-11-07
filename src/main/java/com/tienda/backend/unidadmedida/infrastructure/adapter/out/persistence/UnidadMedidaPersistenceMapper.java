package com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import org.springframework.stereotype.Component;

@Component
public class UnidadMedidaPersistenceMapper {
    public UnidadMedida toDomain(UnidadMedidaEntity entity) {
        if (entity == null) return null;

        return new UnidadMedida(entity.getId(), entity.getNombre(), entity.getAbreviatura());
    }

    public UnidadMedidaEntity toEntity(UnidadMedida domain) {
        if (domain == null) return null;

        return UnidadMedidaEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .abreviatura(domain.getAbreviatura())
                .build();
    }
}
