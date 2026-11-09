package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Filas de los reportes de ventas. Todos los importes van con 2 decimales. "Ventas" cuenta solo las COMPLETADAS;
 * las anuladas se informan aparte. {@code devuelto} es el valor de lo devuelto: el dinero reembolsado más el crédito
 * que se aplicó a la venta de un cambio; {@code neto} = total vendido − devuelto.
 */
public final class FilasVentas {
    private FilasVentas() {
    }

    public record Periodo(LocalDate periodo, long ventas, BigDecimal subtotal, BigDecimal descuentos,
                          BigDecimal total, BigDecimal ticketPromedio, long anuladas, BigDecimal totalAnuladas,
                          long devoluciones, BigDecimal devuelto, BigDecimal neto) {
    }

    /** Unidades en unidad base del producto; {@code importe} es neto de descuentos. */
    public record Producto(Long productoId, String producto, String categoria, BigDecimal unidades, long ventas,
                           BigDecimal descuentos, BigDecimal importe) {
    }

    public record ProductoSinVentas(Long productoId, String producto, String categoria) {
    }

    /** Un cajero o una caja. */
    public record Origen(Long id, String nombre, long ventas, BigDecimal total, BigDecimal ticketPromedio,
                         long anuladas, BigDecimal totalAnuladas, long devoluciones, BigDecimal devuelto) {
    }

    /**
     * {@code cobrado} no incluye el crédito de devolución (no es dinero) y el efectivo va neto del vuelto.
     * {@code reembolsado} es lo devuelto por ese método; en efectivo incluye el saldo que se entrega en los cambios.
     */
    public record MetodoPago(Long metodoPagoId, String metodoPago, long ventas, BigDecimal cobrado,
                             BigDecimal reembolsado, BigDecimal neto) {
    }

    /** Un cliente, un tipo de cliente o las ventas sin cliente ({@code id} nulo). */
    public record Cliente(Long id, String nombre, String documento, long ventas, BigDecimal total,
                          BigDecimal ticketPromedio, LocalDateTime ultimaCompra) {
    }

    /** {@code tipo}: ANULACION, DEVOLUCION o CAMBIO. {@code monto}: total de la venta anulada o valor devuelto. */
    public record Incidencia(String tipo, String numero, String ventaNumero, LocalDateTime fecha, String usuario,
                             String motivo, BigDecimal monto) {
    }
}
