package com.tienda.backend.application.dto.response;

public record ProductoStatsResponse(
        long totalProductos,
        long activos,
        long inactivos
) {
}
