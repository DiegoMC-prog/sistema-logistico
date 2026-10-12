package com.tienda.backend.application.dto.response;

public record DepositoResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean estado
) {
}
