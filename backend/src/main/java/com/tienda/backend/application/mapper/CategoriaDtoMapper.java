package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;
import com.tienda.backend.domain.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaDtoMapper {
    public Categoria toDomain(CategoriaRequest request) {
        return new Categoria(request.nombre());
    }

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(),
                categoria.getNombre(),
                categoria.getCreatedAt(),
                categoria.getUpdatedAt()
        );
    }
}
