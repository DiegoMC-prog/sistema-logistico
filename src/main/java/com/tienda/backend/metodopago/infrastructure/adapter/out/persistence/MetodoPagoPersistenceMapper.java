package com.tienda.backend.metodopago.infrastructure.adapter.out.persistence;

import com.tienda.backend.metodopago.domain.model.EstadoMetodoPago;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import org.springframework.stereotype.Component;

@Component
public class MetodoPagoPersistenceMapper {
    public MetodoPago toDomain(MetodoPagoEntity entity) {
        if (entity == null) return null;
        return new MetodoPago(entity.getId(), entity.getNombre(), EstadoMetodoPago.valueOf(entity.getEstado()));
    }

    public MetodoPagoEntity toEntity(MetodoPago domain) {
        if (domain == null) return null;
        return MetodoPagoEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .estado(domain.getEstado().name())
                .build();
    }
}
