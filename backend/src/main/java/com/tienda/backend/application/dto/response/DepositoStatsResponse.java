package com.tienda.backend.application.dto.response;

public record DepositoStatsResponse(
        long totalDepositos,
        long activos,
        long inactivos
) {
}
