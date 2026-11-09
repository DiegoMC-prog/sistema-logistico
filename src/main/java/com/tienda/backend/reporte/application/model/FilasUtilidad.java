package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Filas de los reportes de utilidad. {@code ingreso} es lo vendido neto de descuentos y {@code costo} el costo real de
 * lo vendido (el costo del lote del que salió cada unidad, guardado en la propia venta). {@code utilidad} = ingreso −
 * costo y {@code margen} = utilidad / ingreso en porcentaje (nulo si no hubo ingreso). Solo ventas COMPLETADAS.
 */
public final class FilasUtilidad {
    private FilasUtilidad() {
    }

    /** Utilidad bruta de un producto: no descuenta devoluciones (no se pueden atribuir a un producto con exactitud). */
    public record Producto(Long productoId, String producto, String categoria, BigDecimal unidades,
                           BigDecimal ingreso, BigDecimal costo, BigDecimal utilidad, BigDecimal margen) {
    }

    /**
     * Utilidad de un período. {@code devuelto} es el valor de lo devuelto y {@code costoRecuperado} el costo de la
     * mercadería devuelta que volvió al stock. {@code utilidadNeta} = (ingreso − devuelto) − (costo − costoRecuperado):
     * lo devuelto en mal estado y sin reingreso al stock queda como pérdida.
     */
    public record Periodo(LocalDate periodo, long ventas, BigDecimal ingreso, BigDecimal costo, BigDecimal utilidad,
                          BigDecimal margen, BigDecimal devuelto, BigDecimal costoRecuperado,
                          BigDecimal utilidadNeta) {
    }

    /**
     * Línea vendida con precio manual. {@code costoUnitario} es por unidad de venta; {@code bajoCosto} indica que se
     * vendió por debajo del costo y {@code perdida} cuánto se perdió en esa línea (cero si no fue bajo costo).
     * Incluye ventas anuladas ({@code estadoVenta}) para poder auditarlas.
     */
    public record PrecioManual(String ventaNumero, String estadoVenta, LocalDateTime fecha, String usuario,
                               String producto, BigDecimal cantidad, BigDecimal precioUnitario,
                               BigDecimal costoUnitario, BigDecimal diferencia, boolean bajoCosto,
                               BigDecimal perdida, BigDecimal subtotal, String motivo) {
    }
}
