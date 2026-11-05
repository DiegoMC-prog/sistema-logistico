package com.tienda.backend.categoria.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.domain.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaPersitenceMapper {
    public Categoria toDomain(CategoriaEntity entity) {
        if (entity == null) return null;

        return new Categoria(
                entity.getId(),
                entity.getNombre()
        );
    }

    public CategoriaEntity toEntity(Categoria categoria) {
        if (categoria == null) return null;

        return CategoriaEntity.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .build();
    }
}
