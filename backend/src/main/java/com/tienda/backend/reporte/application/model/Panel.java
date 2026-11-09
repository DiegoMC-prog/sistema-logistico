package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resumen del estado del negocio para la pantalla de inicio. Cada bloque llega solo si el usuario tiene el permiso
 * del reporte del que sale (ventas, caja, inventario, compras o utilidad); un bloque nulo = sin permiso.
 */
public record Panel(
        LocalDateTime generadoEn,
        Ventas ventas,
        Caja caja,
        Inventario inventario,
        Compras compras,
        Utilidad utilidad
) {
    /** {@code variacionTotal}: % de cambio del total de hoy respecto de ayer (nulo si ayer no hubo ventas). */
    public record Ventas(Dia hoy, Dia ayer, BigDecimal variacionTotal, List<DiaVentas> ultimos7Dias) {
    }

    public record Dia(long ventas, BigDecimal total, BigDecimal ticketPromedio, long anuladas) {
    }

    public record DiaVentas(LocalDate fecha, long ventas, BigDecimal total) {
    }

    public record Caja(long turnosAbiertos, List<TurnoAbierto> turnos) {
    }

    public record TurnoAbierto(Long turnoId, String caja, String cajero, LocalDateTime fechaApertura, long ventas,
                               BigDecimal totalVentas, BigDecimal efectivoEsperado) {
    }

    /** {@code criticos}: los productos más por debajo de su mínimo (hasta 5). */
    public record Inventario(long productosBajoMinimo, long sinStock, List<StockCritico> criticos,
                             long lotesPorVencer7Dias, long lotesVencidos, BigDecimal valorEnRiesgo) {
    }

    public record StockCritico(Long productoId, String producto, BigDecimal stock, BigDecimal stockMinimo,
                               BigDecimal faltante) {
    }

    /** {@code masAtrasados}: los pedidos con más días de atraso (hasta 3). */
    public record Compras(long pedidosPendientes, long atrasados, BigDecimal montoPendiente,
                          List<PedidoAtrasado> masAtrasados) {
    }

    public record PedidoAtrasado(Long pedidoId, String numeroPedido, String proveedor, long diasAtraso,
                                 BigDecimal montoPendiente) {
    }

    /** Utilidad bruta de hoy y de los últimos 7 días (incluido hoy), con su margen en %. */
    public record Utilidad(BigDecimal utilidadHoy, BigDecimal margenHoy, BigDecimal utilidad7Dias,
                           BigDecimal margen7Dias) {
    }
}
