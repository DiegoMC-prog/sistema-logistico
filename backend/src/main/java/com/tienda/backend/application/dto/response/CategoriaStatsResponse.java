package com.tienda.backend.application.dto.response;

public record CategoriaStatsResponse(
        long totalCategorias,
        long categoriasEnUso
) {
}
