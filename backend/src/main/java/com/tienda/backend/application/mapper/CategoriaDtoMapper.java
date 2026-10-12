package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;
import com.tienda.backend.domain.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaDtoMapper {

    public Categoria toDomain(CategoriaRequest request) {
        return Categoria.crear(request.nombre());
    }

    public CategoriaResponse toResponse(Categoria domain) {
        if (domain == null) {
            return null;
        }
        return new CategoriaResponse(
                domain.getId(),
                domain.getNombre(),
                domain.getTotalProductos() != null ? domain.getTotalProductos() : 0L
        );
    }
}
