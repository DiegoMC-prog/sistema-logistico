package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Filas de los reportes de compras. Importes con 2 decimales; cantidades en unidad base. */
public final class FilasCompras {
    private FilasCompras() {
    }

    /**
     * Compras a un proveedor en el rango (por fecha del pedido). Cuentan los pedidos enviados, parcial o totalmente
     * recibidos; los borradores no cuentan y los cancelados se informan aparte. {@code cumplimiento} = recibido /
     * pedido en porcentaje (nulo si no se pidió nada).
     */
    public record Proveedor(Long proveedorId, String proveedor, long pedidos, long cancelados,
                            BigDecimal montoPedido, BigDecimal montoRecibido, BigDecimal montoPendiente,
                            BigDecimal cumplimiento) {
    }

    /**
     * Pedido enviado que aún no se recibió completo. {@code diasAtraso} es cuántos días pasaron de la fecha estimada
     * de entrega (0 si no venció; nulo si el pedido no tiene fecha estimada).
     */
    public record Pendiente(Long pedidoId, String numeroPedido, String estado, Long proveedorId, String proveedor,
                            Long depositoId, String deposito, LocalDate fechaPedido, LocalDate fechaEntregaEstimada,
                            Long diasAtraso, BigDecimal total, long lineasPendientes, BigDecimal unidadesPendientes,
                            BigDecimal montoPendiente) {
    }

    /**
     * Una compra de un producto: lo recibido de un lote a su costo pactado. {@code variacion} es el cambio porcentual
     * del costo respecto de la compra anterior (nulo en la primera).
     */
    public record CostoProducto(LocalDateTime fecha, String numeroPedido, Long proveedorId, String proveedor,
                                String numeroLote, BigDecimal cantidad, BigDecimal costoUnitario,
                                BigDecimal variacion) {
    }
}
