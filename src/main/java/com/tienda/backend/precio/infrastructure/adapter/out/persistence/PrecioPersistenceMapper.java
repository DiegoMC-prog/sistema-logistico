package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.domain.model.EstadoPrecio;
import com.tienda.backend.precio.domain.model.Precio;
import org.springframework.stereotype.Component;

@Component
public class PrecioPersistenceMapper {
    public Precio toDomain(PrecioEntity e) {
        if (e == null) return null;
        return new Precio(e.getId(), e.getPresentacionId(), e.getTipoClienteId(), e.getCantidadMinima(),
                e.getPrecio(), EstadoPrecio.valueOf(e.getEstado()));
    }

    public PrecioEntity toEntity(Precio d) {
        if (d == null) return null;
        return PrecioEntity.builder()
                .id(d.getId())
                .presentacionId(d.getPresentacionId())
                .tipoClienteId(d.getTipoClienteId())
                .cantidadMinima(d.getCantidadMinima())
                .precio(d.getPrecio())
                .estado(d.getEstado().name())
                .build();
    }
}
