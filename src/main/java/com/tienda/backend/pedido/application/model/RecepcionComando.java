package com.tienda.backend.pedido.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Recepción de mercadería de un pedido. Cada línea indica qué línea del pedido se recibe y cuánto, y los datos del lote
 * nuevo que se crea (el número es opcional: si falta, se genera). El costo del lote es el pactado en el pedido.
 */
public record RecepcionComando(
        Long pedidoId,
        String numeroGuia,
        String observacion,
        List<Item> items
) {
    public record Item(Long lineaId, BigDecimal cantidad, String numeroLote, LocalDate fechaVencimiento,
                       BigDecimal porcentajeDescuentoVencimiento) {
    }
}
