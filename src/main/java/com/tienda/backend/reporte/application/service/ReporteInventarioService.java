package com.tienda.backend.reporte.application.service;

import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.reporte.application.model.AgrupacionStock;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteInventarioUseCase;
import com.tienda.backend.reporte.application.port.out.ReporteInventarioRepositoryPort;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReporteInventarioService implements ConsultarReporteInventarioUseCase {
    static final int DIAS_DEFAULT = 30;
    static final int DIAS_MAXIMO = 365;

    private final ReporteInventarioRepositoryPort reporteInventarioRepositoryPort;

    @Override
    public ReporteResultado<FilasInventario.Stock> stockValorizado(AgrupacionStock agrupacion, Long depositoId,
                                                                   Long categoriaId) {
        List<FilasInventario.Stock> filas = reporteInventarioRepositoryPort.stockValorizado(
                agrupacion == null ? AgrupacionStock.PRODUCTO : agrupacion, depositoId, categoriaId);
        BigDecimal stock = cantidades(filas, FilasInventario.Stock::stock);
        BigDecimal valor = suma(filas, FilasInventario.Stock::valor);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("filas", filas.size());
        totales.put("stock", stock);
        totales.put("valor", valor);
        return new ReporteResultado<>(null, null, totales, filas);
    }

    @Override
    public ReporteResultado<FilasInventario.StockBajo> stockBajo(Long depositoId, Long categoriaId) {
        List<FilasInventario.StockBajo> filas = reporteInventarioRepositoryPort.stockBajo(depositoId, categoriaId);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("productos", filas.size());
        totales.put("sinStock", filas.stream().filter(f -> f.stock().signum() == 0).count());
        totales.put("faltante", cantidades(filas, FilasInventario.StockBajo::faltante));
        return new ReporteResultado<>(null, null, totales, filas);
    }

    @Override
    public ReporteResultado<FilasInventario.LoteVencimiento> lotesPorVencer(Integer dias, boolean incluirVencidos,
                                                                            Long depositoId) {
        int ventana = dias == null ? DIAS_DEFAULT : dias;
        if (ventana < 0 || ventana > DIAS_MAXIMO) {
            throw new ReporteParametrosInvalidosException("Los días deben estar entre 0 y " + DIAS_MAXIMO);
        }
        LocalDate hoy = LocalDate.now();
        List<FilasInventario.LoteVencimiento> filas = reporteInventarioRepositoryPort.lotesPorVencer(
                hoy, hoy.plusDays(ventana), incluirVencidos, depositoId);

        List<FilasInventario.LoteVencimiento> vencidos = filas.stream().filter(f -> f.diasRestantes() < 0).toList();
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("lotes", filas.size());
        totales.put("vencidos", vencidos.size());
        totales.put("porVencer", filas.size() - vencidos.size());
        totales.put("stock", cantidades(filas, FilasInventario.LoteVencimiento::stock));
        totales.put("valorEnRiesgo", suma(filas, FilasInventario.LoteVencimiento::valorEnRiesgo));
        totales.put("valorVencido", suma(vencidos, FilasInventario.LoteVencimiento::valorEnRiesgo));
        return new ReporteResultado<>(null, null, totales, filas);
    }

    @Override
    public ReporteResultado<FilasInventario.Kardex> kardex(Long productoId, Long depositoId, RangoFechas rango) {
        if (productoId == null) {
            throw new ReporteParametrosInvalidosException("El producto es obligatorio para el kardex (productoId)");
        }
        reporteInventarioRepositoryPort.nombreProducto(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        BigDecimal saldo = reporteInventarioRepositoryPort.saldoKardexInicial(productoId, depositoId, rango);
        BigDecimal saldoInicial = saldo;
        List<FilasInventario.Kardex> filas = new ArrayList<>();
        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal salidas = BigDecimal.ZERO;
        for (FilasInventario.Kardex m : reporteInventarioRepositoryPort.movimientosKardex(productoId, depositoId, rango)) {
            saldo = saldo.add(m.entrada()).subtract(m.salida());
            entradas = entradas.add(m.entrada());
            salidas = salidas.add(m.salida());
            filas.add(new FilasInventario.Kardex(m.movimientoId(), m.fecha(), m.tipo(), m.depositoOrigen(),
                    m.depositoDestino(), m.usuario(), m.referencia(), m.numeroLote(), m.entrada(), m.salida(), saldo));
        }

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("saldoInicial", cantidad(saldoInicial));
        totales.put("entradas", cantidad(entradas));
        totales.put("salidas", cantidad(salidas));
        totales.put("saldoFinal", cantidad(saldo));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    /** Suma de cantidades en unidad base: hasta 4 decimales, sin ceros de relleno. */
    private static <T> BigDecimal cantidades(List<T> filas, Function<T, BigDecimal> campo) {
        return cantidad(filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal cantidad(BigDecimal valor) {
        BigDecimal v = valor.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0) : v;
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
