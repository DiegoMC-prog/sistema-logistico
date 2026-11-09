package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Filas de los reportes de inventario. Las cantidades van en unidad base del producto y los importes con 2 decimales.
 * El valor de una existencia es {@code stock × costo unitario de compra de su lote}.
 */
public final class FilasInventario {
    private FilasInventario() {
    }

    /**
     * Una fila por producto, depósito o categoría según la agrupación. {@code categoria} solo se informa al agrupar
     * por producto. {@code lotes} cuenta los lotes con existencias.
     */
    public record Stock(Long id, String nombre, String categoria, BigDecimal stock, BigDecimal valor,
                        BigDecimal costoPromedio, long lotes) {
    }

    /**
     * Producto con existencias por debajo del mínimo. {@code vendido30Dias} son las unidades vendidas en los últimos
     * 30 días y {@code diasDeStock} cuántos días alcanza el stock a ese ritmo (nulo si no hubo ventas).
     */
    public record StockBajo(Long productoId, String producto, String categoria, BigDecimal stock,
                            BigDecimal stockMinimo, BigDecimal faltante, BigDecimal vendido30Dias,
                            BigDecimal diasDeStock) {
    }

    /**
     * Lote con existencias que vence pronto o ya venció. {@code diasRestantes} es negativo si ya venció;
     * {@code estado}: VENCIDO o POR_VENCER; {@code valorEnRiesgo} = stock × costo.
     */
    public record LoteVencimiento(Long loteId, String numeroLote, Long productoId, String producto,
                                  LocalDate fechaVencimiento, long diasRestantes, String estado, BigDecimal stock,
                                  BigDecimal costoUnitario, BigDecimal valorEnRiesgo,
                                  BigDecimal descuentoVencimiento) {
    }

    /**
     * Un movimiento del kardex. {@code entrada} y {@code salida} son lo que sumó y restó en el depósito consultado
     * (o en el total si no se eligió depósito: un traslado entre depósitos no cambia el total); {@code saldo} es el
     * acumulado después del movimiento.
     */
    public record Kardex(Long movimientoId, LocalDateTime fecha, String tipo, String depositoOrigen,
                         String depositoDestino, String usuario, String referencia, String numeroLote,
                         BigDecimal entrada, BigDecimal salida, BigDecimal saldo) {
    }
}
