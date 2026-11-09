package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteVentasUseCase;
import com.tienda.backend.reporte.application.port.out.ReporteVentasRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReporteVentasService implements ConsultarReporteVentasUseCase {
    static final int LIMITE_DEFAULT = 100;
    static final int LIMITE_MAXIMO = 5000;

    private final ReporteVentasRepositoryPort reporteVentasRepositoryPort;

    @Override
    public ReporteResultado<FilasVentas.Periodo> resumen(RangoFechas rango, Agrupacion agrupacion) {
        List<FilasVentas.Periodo> filas = reporteVentasRepositoryPort.resumenPorPeriodo(rango,
                agrupacion == null ? Agrupacion.DIA : agrupacion);

        long ventas = suma(filas, FilasVentas.Periodo::ventas);
        BigDecimal total = suma(filas, FilasVentas.Periodo::total);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("ventas", ventas);
        totales.put("subtotal", suma(filas, FilasVentas.Periodo::subtotal));
        totales.put("descuentos", suma(filas, FilasVentas.Periodo::descuentos));
        totales.put("total", total);
        totales.put("ticketPromedio", promedio(total, ventas));
        totales.put("anuladas", suma(filas, FilasVentas.Periodo::anuladas));
        totales.put("totalAnuladas", suma(filas, FilasVentas.Periodo::totalAnuladas));
        totales.put("devoluciones", suma(filas, FilasVentas.Periodo::devoluciones));
        totales.put("devuelto", suma(filas, FilasVentas.Periodo::devuelto));
        totales.put("neto", suma(filas, FilasVentas.Periodo::neto));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasVentas.Producto> porProducto(RangoFechas rango, Long categoriaId, Integer limite) {
        List<FilasVentas.Producto> filas = reporteVentasRepositoryPort.porProducto(rango, categoriaId, limite(limite));
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("productos", filas.size());
        totales.put("unidades", suma(filas, FilasVentas.Producto::unidades));
        totales.put("descuentos", suma(filas, FilasVentas.Producto::descuentos));
        totales.put("importe", suma(filas, FilasVentas.Producto::importe));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasVentas.ProductoSinVentas> productosSinVentas(RangoFechas rango, Long categoriaId,
                                                                              Integer limite) {
        List<FilasVentas.ProductoSinVentas> filas = reporteVentasRepositoryPort.productosSinVentas(rango,
                categoriaId, limite(limite));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), Map.of("productos", filas.size()), filas);
    }

    @Override
    public ReporteResultado<FilasVentas.Origen> porCajero(RangoFechas rango) {
        return origen(rango, reporteVentasRepositoryPort.porCajero(rango));
    }

    @Override
    public ReporteResultado<FilasVentas.Origen> porCaja(RangoFechas rango) {
        return origen(rango, reporteVentasRepositoryPort.porCaja(rango));
    }

    @Override
    public ReporteResultado<FilasVentas.MetodoPago> porMetodoPago(RangoFechas rango) {
        List<FilasVentas.MetodoPago> filas = reporteVentasRepositoryPort.porMetodoPago(rango);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("cobrado", suma(filas, FilasVentas.MetodoPago::cobrado));
        totales.put("reembolsado", suma(filas, FilasVentas.MetodoPago::reembolsado));
        totales.put("neto", suma(filas, FilasVentas.MetodoPago::neto));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasVentas.Cliente> porCliente(RangoFechas rango, boolean agruparPorTipo,
                                                            Integer limite) {
        List<FilasVentas.Cliente> filas = agruparPorTipo
                ? reporteVentasRepositoryPort.porTipoCliente(rango)
                : reporteVentasRepositoryPort.porCliente(rango, limite(limite));
        long ventas = suma(filas, FilasVentas.Cliente::ventas);
        BigDecimal total = suma(filas, FilasVentas.Cliente::total);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("ventas", ventas);
        totales.put("total", total);
        totales.put("ticketPromedio", promedio(total, ventas));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasVentas.Incidencia> incidencias(RangoFechas rango, Integer limite) {
        int max = limite(limite);
        List<FilasVentas.Incidencia> filas = reporteVentasRepositoryPort.incidencias(rango, max);
        Map<String, Object> totales = new LinkedHashMap<>();
        agregarIncidencia(totales, filas, "ANULACION", "anulaciones", "montoAnulaciones");
        agregarIncidencia(totales, filas, "DEVOLUCION", "devoluciones", "montoDevoluciones");
        agregarIncidencia(totales, filas, "CAMBIO", "cambios", "montoCambios");
        // Si se llegó al límite, puede haber más registros que los mostrados
        totales.put("truncado", filas.size() >= max);
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    private static void agregarIncidencia(Map<String, Object> totales, List<FilasVentas.Incidencia> filas, String tipo,
                                          String claveCantidad, String claveMonto) {
        List<FilasVentas.Incidencia> delTipo = filas.stream().filter(f -> f.tipo().equals(tipo)).toList();
        totales.put(claveCantidad, delTipo.size());
        totales.put(claveMonto, suma(delTipo, FilasVentas.Incidencia::monto));
    }

    private ReporteResultado<FilasVentas.Origen> origen(RangoFechas rango, List<FilasVentas.Origen> filas) {
        long ventas = suma(filas, FilasVentas.Origen::ventas);
        BigDecimal total = suma(filas, FilasVentas.Origen::total);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("ventas", ventas);
        totales.put("total", total);
        totales.put("ticketPromedio", promedio(total, ventas));
        totales.put("anuladas", suma(filas, FilasVentas.Origen::anuladas));
        totales.put("totalAnuladas", suma(filas, FilasVentas.Origen::totalAnuladas));
        totales.put("devoluciones", suma(filas, FilasVentas.Origen::devoluciones));
        totales.put("devuelto", suma(filas, FilasVentas.Origen::devuelto));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    private static int limite(Integer pedido) {
        if (pedido == null || pedido < 1) {
            return LIMITE_DEFAULT;
        }
        return Math.min(pedido, LIMITE_MAXIMO);
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private static <T> long suma(List<T> filas, ToLongFunction<T> campo) {
        return filas.stream().mapToLong(campo).sum();
    }

    private static BigDecimal promedio(BigDecimal total, long cantidad) {
        return cantidad == 0 ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }
}
