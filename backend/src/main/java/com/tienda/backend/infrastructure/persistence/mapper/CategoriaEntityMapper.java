package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoriaEntityMapper {
    public CategoriaEntity toEntity(Categoria domain) {
        return  new CategoriaEntity(
                domain.getId(),
                domain.getNombre()
        );
    }

    public Categoria toDomain(CategoriaEntity entity) {
        return  new Categoria(
                entity.getId(),
                entity.getNombre()
        );
    }

}
