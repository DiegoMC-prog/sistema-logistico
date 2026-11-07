package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.infrastructure.adapter.out.persistence.CategoriaEntity;
import com.tienda.backend.producto.domain.model.EstadoProducto;
import com.tienda.backend.producto.domain.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoPersistenceMapper {
    public Producto toDomain(ProductoEntity entity) {
        if  (entity == null) return null;

        Long categoriaId = entity.getCategoria() != null
                ? entity.getCategoria().getId()
                : null;

        EstadoProducto estado = entity.getEstado() != null
                ? EstadoProducto.valueOf(entity.getEstado())
                : EstadoProducto.ACTIVO;

        return new Producto(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                categoriaId,
                entity.getStockMinimo(),
                Boolean.TRUE.equals(entity.getEsPerecedero()),
                estado
        );
    }

    public ProductoEntity toEntity(Producto domain) {
        if (domain == null) {
            return null;
        }

        CategoriaEntity categoria = null;
        if (domain.getCategoriaId() != null) {
            categoria = new CategoriaEntity();
            categoria.setId(domain.getCategoriaId());
        }

        return ProductoEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .categoria(categoria)
                .stockMinimo(domain.getStockMinimo())
                .esPerecedero(domain.isEsPerecedero())
                .estado(domain.getEstado() != null ? domain.getEstado().name() : null)
                .build();
    }
}
