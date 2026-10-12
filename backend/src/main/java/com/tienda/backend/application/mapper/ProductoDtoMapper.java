package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.ProductoRequest;
import com.tienda.backend.application.dto.response.ProductoResponse;
import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.model.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoDtoMapper {

    public Producto toDomain(ProductoRequest request, Categoria categoria) {
        return Producto.crear(
                request.codigoBarras(),
                request.nombre(),
                request.descripcion(),
                request.imagen(),
                categoria
        );
    }

    public ProductoResponse toResponse(Producto domain) {
        return new ProductoResponse(
                domain.getId(),
                domain.getCodigoBarras(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getImagen(),
                domain.getCategoria() != null ? domain.getCategoria().getId() : null,
                domain.getCategoria() != null ? domain.getCategoria().getNombre() : null,
                domain.getEstado()
        );
    }
}
