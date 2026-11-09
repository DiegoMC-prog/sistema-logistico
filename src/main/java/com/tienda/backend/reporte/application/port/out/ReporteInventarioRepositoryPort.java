package com.tienda.backend.reporte.application.port.out;

import com.tienda.backend.reporte.application.model.AgrupacionStock;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Consultas de agregación sobre inventario, lotes y movimientos. Solo lectura. */
public interface ReporteInventarioRepositoryPort {
    /** Existencias actuales con su valor al costo. Solo filas con stock mayor que cero. */
    List<FilasInventario.Stock> stockValorizado(AgrupacionStock agrupacion, Long depositoId, Long categoriaId);

    /** Productos activos con mínimo definido cuyo stock (total o del depósito) está por debajo del mínimo. */
    List<FilasInventario.StockBajo> stockBajo(Long depositoId, Long categoriaId);

    /** Lotes con existencias cuyo vencimiento es hasta {@code hasta} (inclusive); incluye los ya vencidos si se pide. */
    List<FilasInventario.LoteVencimiento> lotesPorVencer(LocalDate hoy, LocalDate hasta, boolean incluirVencidos,
                                                         Long depositoId);

    Optional<String> nombreProducto(Long productoId);

    /** Saldo del producto (o del depósito) justo antes de {@code rango.inicio()}. */
    BigDecimal saldoKardexInicial(Long productoId, Long depositoId, RangoFechas rango);

    /** Movimientos del rango en orden cronológico, con {@code saldo} aún sin calcular (cero). */
    List<FilasInventario.Kardex> movimientosKardex(Long productoId, Long depositoId, RangoFechas rango);
}
