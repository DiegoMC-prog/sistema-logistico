package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.model.Producto;
import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import com.tienda.backend.infrastructure.persistence.entity.ProductoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductoEntityMapper {

    private final CategoriaEntityMapper categoriaEntityMapper;

    public ProductoEntityMapper(CategoriaEntityMapper categoriaEntityMapper) {
        this.categoriaEntityMapper = categoriaEntityMapper;
    }

    public ProductoEntity toEntity(Producto domain) {
        if (domain == null) {
            return null;
        }

        CategoriaEntity categoriaEntity = domain.getCategoria() != null
                ? categoriaEntityMapper.toEntity(domain.getCategoria())
                : null;

        return new ProductoEntity(
                domain.getId(),
                domain.getCodigoBarras(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getImagen(),
                categoriaEntity,
                domain.getEstado()
        );
    }

    public Producto toDomain(ProductoEntity entity) {
        if (entity == null) {
            return null;
        }

        Categoria categoria = entity.getCategoria() != null
                ? categoriaEntityMapper.toDomain(entity.getCategoria())
                : null;

        return new Producto(
                entity.getId(),
                entity.getCodigoBarras(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getImagen(),
                categoria,
                entity.getEstado()
        );
    }
}
