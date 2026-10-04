package com.tienda.backend.application.dto.response;

import java.time.LocalDateTime;

public record DepositoResponse(
        Long id,
        String nombre,
        String descripcion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
