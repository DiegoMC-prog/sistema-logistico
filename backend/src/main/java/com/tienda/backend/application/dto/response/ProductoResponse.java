package com.tienda.backend.application.dto.response;

public record ProductoResponse(
        Long id,
        String codigoBarras,
        String nombre,
        String descripcion,
        String imagen,
        Long categoriaId,
        String categoriaNombre,
        Boolean estado
) {
}
