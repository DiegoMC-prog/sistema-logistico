package com.tienda.backend.venta.application.service;

import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.PresentacionVenta;
import com.tienda.backend.venta.domain.model.LineaVenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Función crítica 2: armado de líneas de venta con precio, descuento por vencimiento y subtotal. */
class ArmadorLineasTest {

    /** Presentación "unidad" (factor 1) del producto 10. */
    private static final PresentacionVenta UNIDAD = new PresentacionVenta(1L, 10L, "Yogur", true, BigDecimal.ONE);

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    private static AsignacionStock lote(Long loteId, String cantidad, String descuento) {
        return new AsignacionStock(0, loteId, d(cantidad), d("4.00"), d(descuento));
    }

    @Test
    void precioDeListaSinDescuento() {
        // Arrange
        var item = new ArmadorLineas.ItemResuelto(UNIDAD, d("2"), d("10.00"), false, null);

        // Act
        List<LineaVenta> lineas = ArmadorLineas.armar(item, List.of(lote(100L, "2", "0")));

        // Assert
        assertEquals(1, lineas.size());
        assertEquals(d("0.00"), lineas.get(0).descuento());
        assertEquals(d("20.00"), lineas.get(0).subtotal());
    }

    @Test
    void descuentoPorVencimientoSeAplica() {
        // Arrange: lote con 20 % de descuento por estar próximo a vencer
        var item = new ArmadorLineas.ItemResuelto(UNIDAD, d("3"), d("10.00"), false, null);

        // Act
        List<LineaVenta> lineas = ArmadorLineas.armar(item, List.of(lote(100L, "3", "20")));

        // Assert: bruto 30.00 - 20 % = 24.00
        assertEquals(1, lineas.size());
        assertEquals(d("6.00"), lineas.get(0).descuento());
        assertEquals(d("24.00"), lineas.get(0).subtotal());
    }

    @Test
    void lotesConDistintoDescuentoGeneranUnaLineaPorPorcentaje() {
        // Arrange: 2 u. de un lote sin descuento y 1 u. de un lote con 50 %
        var item = new ArmadorLineas.ItemResuelto(UNIDAD, d("3"), d("10.00"), false, null);
        var asignaciones = List.of(lote(100L, "2", "0"), lote(200L, "1", "50"));

        // Act
        List<LineaVenta> lineas = ArmadorLineas.armar(item, asignaciones);

        // Assert
        assertEquals(2, lineas.size());
        assertEquals(0, d("2").compareTo(lineas.get(0).cantidad()));
        assertEquals(d("20.00"), lineas.get(0).subtotal());
        assertEquals(0, d("1").compareTo(lineas.get(1).cantidad()));
        assertEquals(d("5.00"), lineas.get(1).descuento());
        assertEquals(d("5.00"), lineas.get(1).subtotal());
    }

    @Test
    void precioManualNoAplicaDescuentoYVaEnUnaSolaLinea() {
        // Arrange: el cajero fija 8.00 manualmente; los lotes tienen descuentos distintos
        var item = new ArmadorLineas.ItemResuelto(UNIDAD, d("3"), d("8.00"), true, "Oferta del día");
        var asignaciones = List.of(lote(100L, "2", "0"), lote(200L, "1", "50"));

        // Act
        List<LineaVenta> lineas = ArmadorLineas.armar(item, asignaciones);

        // Assert
        assertEquals(1, lineas.size());
        LineaVenta linea = lineas.get(0);
        assertTrue(linea.esPrecioModificado());
        assertEquals(d("0.00"), linea.descuento());
        assertEquals(d("24.00"), linea.subtotal());
        assertEquals(2, linea.lotes().size());
    }

    @Test
    void redondeaLosImportesADosDecimales() {
        // Arrange: 3 x 3.335 = 10.005 -> 10.01 (HALF_UP)
        var item = new ArmadorLineas.ItemResuelto(UNIDAD, d("3"), d("3.335"), false, null);

        // Act
        List<LineaVenta> lineas = ArmadorLineas.armar(item, List.of(lote(100L, "3", "0")));

        // Assert
        assertEquals(d("10.01"), lineas.get(0).subtotal());
        assertFalse(lineas.get(0).esPrecioModificado());
    }
}
