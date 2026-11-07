package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.producto.infrastructure.adapter.out.persistence.ProductoEntity;
import org.springframework.stereotype.Component;

@Component
public class LotePersistenceMapper {
    public Lote toDomain(LoteEntity entity) {
        if (entity == null) return null;

        return new Lote(
                entity.getId(),
                entity.getProducto().getId(),
                entity.getNumeroLote(),
                entity.getFechaVencimiento(),
                entity.getCostoUnitarioCompra(),
                entity.getPorcentajeDescuentoVencimiento()
        );
    }

    /** Requiere que el producto esté cargado (entity graph); lee su nombre. */
    public LoteDetalle toDetalle(LoteEntity entity) {
        return new LoteDetalle(toDomain(entity), entity.getProducto().getNombre());
    }

    public LoteEntity toEntity(Lote domain) {
        if (domain == null) return null;

        ProductoEntity producto = new ProductoEntity();
        producto.setId(domain.getProductoId());

        return LoteEntity.builder()
                .id(domain.getId())
                .producto(producto)
                .numeroLote(domain.getNumeroLote())
                .fechaVencimiento(domain.getFechaVencimiento())
                .costoUnitarioCompra(domain.getCostoUnitarioCompra())
                .porcentajeDescuentoVencimiento(domain.getPorcentajeDescuentoVencimiento())
                .build();
    }
}
