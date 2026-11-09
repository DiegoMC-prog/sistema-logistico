package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.StockDetalle;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockResponse(
        Long id,
        DepositoResumen deposito,
        ProductoResumen producto,
        LoteResumen lote,
        BigDecimal stockActual,
        String unidadBase
) {
    public record DepositoResumen(Long id, String nombre) {
    }

    public record ProductoResumen(Long id, String nombre) {
    }

    public record LoteResumen(Long id, String numeroLote, LocalDate fechaVencimiento) {
    }

    public static StockResponse from(StockDetalle s) {
        return new StockResponse(
                s.inventarioId(),
                new DepositoResumen(s.depositoId(), s.depositoNombre()),
                new ProductoResumen(s.productoId(), s.productoNombre()),
                new LoteResumen(s.loteId(), s.numeroLote(), s.fechaVencimiento()),
                s.stockActual(),
                s.unidadBase());
    }
}
