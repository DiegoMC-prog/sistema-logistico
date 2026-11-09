package com.tienda.backend.pedido.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockPedidoPort {
    /**
     * Ingresa mercadería al depósito creando un lote nuevo por línea. Devuelve el id del lote creado de cada línea,
     * en el mismo orden de {@code items}.
     */
    List<Long> ingresar(Long depositoId, String observacion, Long pedidoId, List<ItemIngreso> items);

    record ItemIngreso(Long productoId, BigDecimal cantidad, String numeroLote, LocalDate fechaVencimiento,
                       BigDecimal costoUnitario, BigDecimal porcentajeDescuentoVencimiento) {
    }
}
