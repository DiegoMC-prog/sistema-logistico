package com.tienda.backend.application.dto.response;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Long id,
        String nombre,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}