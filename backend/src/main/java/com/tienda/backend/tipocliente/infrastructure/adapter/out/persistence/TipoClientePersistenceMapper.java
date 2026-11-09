package com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.tipocliente.domain.model.EstadoTipoCliente;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import org.springframework.stereotype.Component;

@Component
public class TipoClientePersistenceMapper {
    public TipoCliente toDomain(TipoClienteEntity entity) {
        if (entity == null) return null;
        return new TipoCliente(entity.getId(), entity.getNombre(), EstadoTipoCliente.valueOf(entity.getEstado()));
    }

    public TipoClienteEntity toEntity(TipoCliente domain) {
        if (domain == null) return null;
        return TipoClienteEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .estado(domain.getEstado().name())
                .build();
    }
}
