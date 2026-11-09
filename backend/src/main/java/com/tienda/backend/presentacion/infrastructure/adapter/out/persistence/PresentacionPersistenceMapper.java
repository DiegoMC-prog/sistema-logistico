package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.domain.model.Presentacion;
import com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence.UnidadMedidaEntity;
import org.springframework.stereotype.Component;

@Component
public class PresentacionPersistenceMapper {
    public Presentacion toDomain(PresentacionEntity entity) {
        if (entity == null) return null;

        return new Presentacion(
                entity.getId(),
                entity.getProductoId(),
                entity.getUnidadMedida().getId(),
                entity.getCodigoBarras(),
                entity.getFactorConversion(),
                entity.isEsUnidadBase()
        );
    }

    /** Requiere que la unidad esté cargada (entity graph); lee su nombre y abreviatura. */
    public PresentacionDetalle toDetalle(PresentacionEntity entity) {
        return new PresentacionDetalle(
                toDomain(entity),
                entity.getUnidadMedida().getNombre(),
                entity.getUnidadMedida().getAbreviatura()
        );
    }

    public PresentacionEntity toEntity(Presentacion domain) {
        if (domain == null) return null;

        UnidadMedidaEntity unidad = new UnidadMedidaEntity();
        unidad.setId(domain.getUnidadMedidaId());

        return PresentacionEntity.builder()
                .id(domain.getId())
                .productoId(domain.getProductoId())
                .unidadMedida(unidad)
                .codigoBarras(domain.getCodigoBarras())
                .factorConversion(domain.getFactorConversion())
                .esUnidadBase(domain.isEsUnidadBase())
                .build();
    }
}
