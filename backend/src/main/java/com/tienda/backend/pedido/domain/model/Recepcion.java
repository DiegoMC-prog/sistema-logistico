package com.tienda.backend.pedido.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/** Una entrega (total o parcial) de un pedido: qué se recibió de cada línea y en qué lote quedó. */
public record Recepcion(
        Long id,
        Long pedidoId,
        Long usuarioId,
        LocalDateTime fechaRecepcion,
        String numeroGuia,
        String observacion,
        List<LineaRecepcion> lineas
) {
    public record LineaRecepcion(Long id, Long pedidoDetalleId, Long loteId, java.math.BigDecimal cantidad) {
    }
}
