package com.tienda.backend.inventario.application.model;

import com.tienda.backend.inventario.domain.model.Movimiento;

import java.math.BigDecimal;
import java.util.List;

/** Modelo de lectura: el movimiento con los nombres de depósitos, usuario, productos y lotes. */
public record MovimientoDetallado(
        Movimiento movimiento,
        String depositoOrigenNombre,
        String depositoDestinoNombre,
        String usuarioNombre,
        List<LineaDetallada> lineas
) {
    public record LineaDetallada(Long productoId, String productoNombre, Long loteId, String numeroLote,
                                 BigDecimal cantidad) {
    }
}
