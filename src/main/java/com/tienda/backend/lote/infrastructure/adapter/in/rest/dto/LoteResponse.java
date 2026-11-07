package com.tienda.backend.lote.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.domain.model.Lote;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteResponse(
        Long id,
        ProductoResumen producto,
        String numeroLote,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitarioCompra,
        BigDecimal porcentajeDescuentoVencimiento
) {
    public record ProductoResumen(Long id, String nombre) {
    }

    public static LoteResponse from(LoteDetalle detalle) {
        Lote l = detalle.lote();
        return new LoteResponse(
                l.getId(),
                new ProductoResumen(l.getProductoId(), detalle.productoNombre()),
                l.getNumeroLote(),
                l.getFechaVencimiento(),
                l.getCostoUnitarioCompra(),
                l.getPorcentajeDescuentoVencimiento());
    }
}
