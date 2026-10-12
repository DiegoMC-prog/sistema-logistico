package com.tienda.backend.application.dto.response;

public record ProveedorStatsResponse(
        long totalProveedores,
        long activos,
        long inactivos
) {
}
