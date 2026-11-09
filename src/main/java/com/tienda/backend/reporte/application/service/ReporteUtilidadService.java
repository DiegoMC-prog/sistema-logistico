package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteUtilidadUseCase;
import com.tienda.backend.reporte.application.port.out.ReporteUtilidadRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReporteUtilidadService implements ConsultarReporteUtilidadUseCase {
    static final int LIMITE_DEFAULT = 100;
    static final int LIMITE_MAXIMO = 5000;

    private final ReporteUtilidadRepositoryPort reporteUtilidadRepositoryPort;

    @Override
    public ReporteResultado<FilasUtilidad.Producto> porProducto(RangoFechas rango, Long categoriaId, Integer limite) {
        List<FilasUtilidad.Producto> filas = reporteUtilidadRepositoryPort.porProducto(rango, categoriaId, limite(limite));
        BigDecimal ingreso = suma(filas, FilasUtilidad.Producto::ingreso);
        BigDecimal costo = suma(filas, FilasUtilidad.Producto::costo);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("productos", filas.size());
        totales.put("ingreso", ingreso);
        totales.put("costo", costo);
        totales.put("utilidad", ingreso.subtract(costo));
        totales.put("margen", margen(ingreso.subtract(costo), ingreso));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasUtilidad.Periodo> porPeriodo(RangoFechas rango, Agrupacion agrupacion) {
        List<FilasUtilidad.Periodo> filas = reporteUtilidadRepositoryPort.porPeriodo(rango,
                agrupacion == null ? Agrupacion.DIA : agrupacion);
        BigDecimal ingreso = suma(filas, FilasUtilidad.Periodo::ingreso);
        BigDecimal costo = suma(filas, FilasUtilidad.Periodo::costo);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("ventas", filas.stream().mapToLong(FilasUtilidad.Periodo::ventas).sum());
        totales.put("ingreso", ingreso);
        totales.put("costo", costo);
        totales.put("utilidad", ingreso.subtract(costo));
        totales.put("margen", margen(ingreso.subtract(costo), ingreso));
        totales.put("devuelto", suma(filas, FilasUtilidad.Periodo::devuelto));
        totales.put("costoRecuperado", suma(filas, FilasUtilidad.Periodo::costoRecuperado));
        totales.put("utilidadNeta", suma(filas, FilasUtilidad.Periodo::utilidadNeta));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasUtilidad.PrecioManual> preciosManuales(RangoFechas rango, Integer limite) {
        int max = limite(limite);
        List<FilasUtilidad.PrecioManual> filas = reporteUtilidadRepositoryPort.preciosManuales(rango, max);
        // La pérdida cuenta solo las ventas que siguen vigentes
        List<FilasUtilidad.PrecioManual> vigentes = filas.stream().filter(f -> "COMPLETADA".equals(f.estadoVenta())).toList();
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("lineas", filas.size());
        totales.put("bajoCosto", vigentes.stream().filter(FilasUtilidad.PrecioManual::bajoCosto).count());
        totales.put("perdidaBajoCosto", suma(vigentes, FilasUtilidad.PrecioManual::perdida));
        totales.put("importe", suma(vigentes, FilasUtilidad.PrecioManual::subtotal));
        totales.put("truncado", filas.size() >= max);
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    private static int limite(Integer pedido) {
        return pedido == null || pedido < 1 ? LIMITE_DEFAULT : Math.min(pedido, LIMITE_MAXIMO);
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    /** Utilidad sobre ingreso, en porcentaje; nulo si no hubo ingreso. */
    private static BigDecimal margen(BigDecimal utilidad, BigDecimal ingreso) {
        return ingreso.signum() == 0 ? null
                : utilidad.multiply(BigDecimal.valueOf(100)).divide(ingreso, 2, RoundingMode.HALF_UP);
    }
}
