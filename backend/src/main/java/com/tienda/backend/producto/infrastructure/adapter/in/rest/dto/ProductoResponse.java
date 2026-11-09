package com.tienda.backend.producto.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.domain.model.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        CategoriaResumen categoria,
        BigDecimal stockMinimo,
        boolean esPerecedero,
        String estado
) {
    public record CategoriaResumen(Long id, String nombre) {
    }

    public static ProductoResponse from(ProductoDetalle detalle) {
        Producto p = detalle.producto();
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(),
                new CategoriaResumen(p.getCategoriaId(), detalle.categoriaNombre()),
                p.getStockMinimo(), p.isEsPerecedero(), p.getEstado().name());
    }
}
