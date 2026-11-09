package com.tienda.backend.reporte.application.service;

import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteComprasUseCase;
import com.tienda.backend.reporte.application.port.out.ReporteComprasRepositoryPort;
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
public class ReporteComprasService implements ConsultarReporteComprasUseCase {

    private final ReporteComprasRepositoryPort reporteComprasRepositoryPort;

    @Override
    public ReporteResultado<FilasCompras.Proveedor> porProveedor(RangoFechas rango) {
        List<FilasCompras.Proveedor> filas = reporteComprasRepositoryPort.porProveedor(rango);
        BigDecimal pedido = suma(filas, FilasCompras.Proveedor::montoPedido);
        BigDecimal recibido = suma(filas, FilasCompras.Proveedor::montoRecibido);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("proveedores", filas.size());
        totales.put("pedidos", filas.stream().mapToLong(FilasCompras.Proveedor::pedidos).sum());
        totales.put("cancelados", filas.stream().mapToLong(FilasCompras.Proveedor::cancelados).sum());
        totales.put("montoPedido", pedido);
        totales.put("montoRecibido", recibido);
        totales.put("montoPendiente", pedido.subtract(recibido));
        totales.put("cumplimiento", porcentaje(recibido, pedido));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasCompras.Pendiente> pendientes(Long proveedorId, Long depositoId) {
        List<FilasCompras.Pendiente> filas = reporteComprasRepositoryPort.pendientes(LocalDate.now(), proveedorId, depositoId);
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("pedidos", filas.size());
        totales.put("atrasados", filas.stream().filter(f -> f.diasAtraso() != null && f.diasAtraso() > 0).count());
        totales.put("sinFechaEstimada", filas.stream().filter(f -> f.diasAtraso() == null).count());
        totales.put("unidadesPendientes", unidades(filas, FilasCompras.Pendiente::unidadesPendientes));
        totales.put("montoPendiente", suma(filas, FilasCompras.Pendiente::montoPendiente));
        return new ReporteResultado<>(null, null, totales, filas);
    }

    @Override
    public ReporteResultado<FilasCompras.CostoProducto> evolucionCosto(Long productoId, Long proveedorId,
                                                                       RangoFechas rango) {
        if (productoId == null) {
            throw new ReporteParametrosInvalidosException("El producto es obligatorio para la evolución del costo (productoId)");
        }
        reporteComprasRepositoryPort.nombreProducto(productoId).orElseThrow(() -> new ProductoNotFoundException(productoId));

        List<FilasCompras.CostoProducto> filas = new ArrayList<>();
        BigDecimal anterior = null;
        BigDecimal unidades = BigDecimal.ZERO;
        BigDecimal gastado = BigDecimal.ZERO;
        BigDecimal minimo = null;
        BigDecimal maximo = null;
        for (FilasCompras.CostoProducto c : reporteComprasRepositoryPort.comprasDeProducto(productoId, proveedorId, rango)) {
            BigDecimal variacion = anterior == null || anterior.signum() == 0 ? null
                    : c.costoUnitario().subtract(anterior).multiply(BigDecimal.valueOf(100))
                            .divide(anterior, 2, RoundingMode.HALF_UP);
            filas.add(new FilasCompras.CostoProducto(c.fecha(), c.numeroPedido(), c.proveedorId(), c.proveedor(),
                    c.numeroLote(), c.cantidad(), c.costoUnitario(), variacion));
            anterior = c.costoUnitario();
            unidades = unidades.add(c.cantidad());
            gastado = gastado.add(c.cantidad().multiply(c.costoUnitario()));
            minimo = minimo == null ? c.costoUnitario() : minimo.min(c.costoUnitario());
            maximo = maximo == null ? c.costoUnitario() : maximo.max(c.costoUnitario());
        }

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("compras", filas.size());
        totales.put("unidades", unidades.stripTrailingZeros().scale() < 0 ? unidades.setScale(0) : unidades.stripTrailingZeros());
        totales.put("gastado", gastado.setScale(2, RoundingMode.HALF_UP));
        // Costo promedio ponderado por las unidades de cada compra
        totales.put("costoPromedio", unidades.signum() == 0 ? null : gastado.divide(unidades, 2, RoundingMode.HALF_UP));
        totales.put("costoMinimo", minimo);
        totales.put("costoMaximo", maximo);
        totales.put("costoUltimo", filas.isEmpty() ? null : filas.get(filas.size() - 1).costoUnitario());
        BigDecimal primero = filas.isEmpty() ? null : filas.get(0).costoUnitario();
        totales.put("variacionTotal", primero == null || primero.signum() == 0 || filas.size() < 2 ? null
                : filas.get(filas.size() - 1).costoUnitario().subtract(primero).multiply(BigDecimal.valueOf(100))
                        .divide(primero, 2, RoundingMode.HALF_UP));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    /** Suma de cantidades en unidad base: hasta 4 decimales, sin ceros de relleno. */
    private static <T> BigDecimal unidades(List<T> filas, Function<T, BigDecimal> campo) {
        BigDecimal v = filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0) : v;
    }

    private static BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        return total.signum() == 0 ? null
                : parte.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }
}
