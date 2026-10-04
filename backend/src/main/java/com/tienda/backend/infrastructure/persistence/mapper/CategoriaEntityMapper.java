package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoriaEntityMapper {

    public CategoriaEntity toEntity(Categoria categoria) {
        return new CategoriaEntity(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getCreatedAt(),
                categoria.getUpdatedAt()
        );
    }

    public Categoria toDomain(CategoriaEntity entity) {
        return new Categoria(
                entity.getId(),
                entity.getNombre(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
