package com.tienda.backend.devolucion.application.service;

import com.tienda.backend.devolucion.application.model.ClaveLinea;
import com.tienda.backend.devolucion.application.model.ItemDevueltoComando;
import com.tienda.backend.devolucion.application.model.ReposicionStock;
import com.tienda.backend.devolucion.application.model.VentaOriginal;
import com.tienda.backend.devolucion.application.model.VentaOriginal.LineaOriginal;
import com.tienda.backend.devolucion.application.model.VentaOriginal.LoteOriginal;
import com.tienda.backend.devolucion.domain.exception.CantidadExcedeLoVendidoException;
import com.tienda.backend.devolucion.domain.exception.DevolucionDatosInvalidosException;
import com.tienda.backend.devolucion.domain.model.LineaDevuelta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reparte lo devuelto entre las líneas y lotes de la venta y calcula cuánto vale. Es lógica pura (sin acceso a datos).
 * <ul>
 *   <li>Lotes: lo devuelto vuelve a los lotes de los que salió la línea, en el orden en que salieron, sin pasarse de lo
 *       vendido en cada uno menos lo ya devuelto.</li>
 *   <li>Valor: lo que el cliente pagó por esas unidades (precio de la línea con su descuento por vencimiento). Se calcula
 *       de forma acumulada, así que devolver de a poco no pierde ni sobra centavos y devolver todo vale exactamente el
 *       subtotal de la línea.</li>
 * </ul>
 */
final class CalculadorDevolucion {
    private static final int ESCALA = 4;

    private CalculadorDevolucion() {
    }

    record Calculo(List<LineaDevuelta> lineas, List<ReposicionStock> reposiciones, BigDecimal monto) {
    }

    static Calculo calcular(VentaOriginal venta, List<ItemDevueltoComando> items, Map<ClaveLinea, BigDecimal> devuelto) {
        Map<Long, LineaOriginal> lineasPorId = new HashMap<>();
        venta.lineas().forEach(l -> lineasPorId.put(l.ventaDetalleId(), l));

        List<LineaDevuelta> lineas = new ArrayList<>();
        Map<Long, ReposicionStock> reposiciones = new LinkedHashMap<>();
        BigDecimal monto = BigDecimal.ZERO.setScale(2);

        for (ItemDevueltoComando item : items) {
            LineaOriginal linea = lineasPorId.get(item.ventaDetalleId());
            if (linea == null) {
                throw new DevolucionDatosInvalidosException(
                        "La línea " + item.ventaDetalleId() + " no pertenece a la venta " + venta.numeroVenta());
            }
            BigDecimal base = item.cantidad().multiply(linea.factorConversion()).setScale(ESCALA, RoundingMode.HALF_UP);
            if (base.signum() <= 0) {
                throw new DevolucionDatosInvalidosException("La cantidad devuelta es demasiado pequeña");
            }

            BigDecimal vendidoBase = linea.lotes().stream().map(LoteOriginal::cantidadBase)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal antes = linea.lotes().stream()
                    .map(l -> devuelto.getOrDefault(new ClaveLinea(linea.ventaDetalleId(), l.loteId()), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal quedaPorDevolver = vendidoBase.subtract(antes);
            if (base.compareTo(quedaPorDevolver) > 0) {
                throw new CantidadExcedeLoVendidoException(linea.ventaDetalleId(),
                        quedaPorDevolver.divide(linea.factorConversion(), ESCALA, RoundingMode.DOWN));
            }

            // Reparto entre lotes
            BigDecimal falta = base;
            for (LoteOriginal lote : linea.lotes()) {
                if (falta.signum() <= 0) {
                    break;
                }
                BigDecimal disponible = lote.cantidadBase().subtract(
                        devuelto.getOrDefault(new ClaveLinea(linea.ventaDetalleId(), lote.loteId()), BigDecimal.ZERO));
                if (disponible.signum() <= 0) {
                    continue;
                }
                BigDecimal tomar = disponible.min(falta);
                lineas.add(new LineaDevuelta(null, linea.ventaDetalleId(), lote.loteId(), tomar, item.reingresaStock()));
                if (item.reingresaStock()) {
                    reposiciones.merge(lote.loteId(), new ReposicionStock(linea.productoId(), lote.loteId(), tomar),
                            (a, b) -> new ReposicionStock(a.productoId(), a.loteId(), a.cantidadBase().add(b.cantidadBase())));
                }
                falta = falta.subtract(tomar);
            }

            // Valor acumulado: lo que vale lo devuelto hasta ahora menos lo que valía antes de esta devolución
            monto = monto.add(valor(linea.subtotal(), antes.add(base), vendidoBase)
                    .subtract(valor(linea.subtotal(), antes, vendidoBase)));
        }
        return new Calculo(lineas, List.copyOf(reposiciones.values()), monto);
    }

    private static BigDecimal valor(BigDecimal subtotal, BigDecimal devuelto, BigDecimal vendido) {
        return subtotal.multiply(devuelto).divide(vendido, 8, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }
}
