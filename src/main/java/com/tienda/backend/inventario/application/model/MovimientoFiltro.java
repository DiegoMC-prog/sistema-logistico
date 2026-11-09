package com.tienda.backend.inventario.application.model;

import com.tienda.backend.inventario.domain.model.TipoMovimiento;

import java.time.LocalDate;

/** Criterios opcionales del historial. {@code depositoId} coincide con el origen o el destino; las fechas son inclusivas. */
public record MovimientoFiltro(
        TipoMovimiento tipo,
        Long depositoId,
        Long productoId,
        Long loteId,
        LocalDate desde,
        LocalDate hasta
) {
}
