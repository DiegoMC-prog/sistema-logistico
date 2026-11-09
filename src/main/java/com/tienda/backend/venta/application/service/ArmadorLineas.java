package com.tienda.backend.venta.application.service;

import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.PresentacionVenta;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.LoteVendido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte una línea pedida y los lotes de los que salió en las líneas de la venta. Es lógica pura (sin acceso a datos).
 * <ul>
 *   <li>Precio de lista: si el reparto cruza lotes con distinto descuento por vencimiento, se divide en una línea por
 *       cada porcentaje, de modo que cada línea tiene un único precio y descuento.</li>
 *   <li>Precio manual: es el precio final; no se aplica además el descuento por vencimiento y va en una sola línea.</li>
 * </ul>
 * Los importes se redondean a 2 decimales por línea.
 */
final class ArmadorLineas {
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private ArmadorLineas() {
    }

    /** Una línea pedida ya validada y con su precio resuelto. */
    record ItemResuelto(PresentacionVenta presentacion, BigDecimal cantidad, BigDecimal precio, boolean manual,
                        String motivoPrecio) {
    }

    static List<LineaVenta> armar(ItemResuelto item, List<AsignacionStock> asignaciones) {
        PresentacionVenta pres = item.presentacion();
        if (item.manual()) {
            BigDecimal bruto = dinero(item.precio().multiply(item.cantidad()));
            return List.of(new LineaVenta(null, pres.productoId(), pres.presentacionId(), item.cantidad(),
                    pres.factorConversion(), item.precio(), dinero(BigDecimal.ZERO), bruto, true, item.motivoPrecio(),
                    lotes(asignaciones)));
        }

        Map<BigDecimal, List<AsignacionStock>> grupos = new LinkedHashMap<>();
        for (AsignacionStock a : asignaciones) {
            grupos.computeIfAbsent(a.porcentajeDescuento().setScale(4, RoundingMode.HALF_UP), k -> new ArrayList<>()).add(a);
        }

        List<LineaVenta> lineas = new ArrayList<>();
        BigDecimal restante = item.cantidad();
        int i = 0;
        for (Map.Entry<BigDecimal, List<AsignacionStock>> grupo : grupos.entrySet()) {
            boolean ultimo = ++i == grupos.size();
            BigDecimal baseGrupo = grupo.getValue().stream().map(AsignacionStock::cantidadBase)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cantidad = ultimo ? restante : baseGrupo.divide(pres.factorConversion(), 4, RoundingMode.HALF_UP);
            if (cantidad.signum() <= 0) {
                cantidad = baseGrupo.divide(pres.factorConversion(), 4, RoundingMode.HALF_UP);
            }
            restante = restante.subtract(cantidad);

            BigDecimal bruto = dinero(item.precio().multiply(cantidad));
            BigDecimal descuento = dinero(bruto.multiply(grupo.getKey()).divide(CIEN, 6, RoundingMode.HALF_UP));
            lineas.add(new LineaVenta(null, pres.productoId(), pres.presentacionId(), cantidad, pres.factorConversion(),
                    item.precio(), descuento, bruto.subtract(descuento), false, null, lotes(grupo.getValue())));
        }
        return lineas;
    }

    private static List<LoteVendido> lotes(List<AsignacionStock> asignaciones) {
        return asignaciones.stream()
                .map(a -> new LoteVendido(null, a.loteId(), a.cantidadBase(), a.costoUnitario()))
                .toList();
    }

    static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
