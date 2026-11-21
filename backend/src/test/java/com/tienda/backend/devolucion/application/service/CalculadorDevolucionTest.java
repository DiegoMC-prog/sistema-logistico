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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Función crítica 4: cálculo del reembolso y reparto por lotes de una devolución. */
class CalculadorDevolucionTest {

    private static final Long LINEA = 50L;
    private static final Long PRODUCTO = 10L;

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    /** Venta con una línea de 3 unidades que costó 10.00 en total, salida de los lotes indicados. */
    private static VentaOriginal venta(LoteOriginal... lotes) {
        LineaOriginal linea = new LineaOriginal(LINEA, PRODUCTO, 1L, d("3"), BigDecimal.ONE, d("10.00"), List.of(lotes));
        return new VentaOriginal(1L, "V-00000001", false, null, 1L, LocalDateTime.now(), List.of(linea));
    }

    @Test
    void devolucionTotalValeExactamenteElSubtotalDeLaLinea() {
        // Arrange
        VentaOriginal venta = venta(new LoteOriginal(100L, d("3")));
        var items = List.of(new ItemDevueltoComando(LINEA, d("3"), true));

        // Act
        CalculadorDevolucion.Calculo calculo = CalculadorDevolucion.calcular(venta, items, Map.of());

        // Assert
        assertEquals(d("10.00"), calculo.monto());
        assertEquals(1, calculo.reposiciones().size());
        ReposicionStock repo = calculo.reposiciones().get(0);
        assertEquals(100L, repo.loteId());
        assertEquals(0, d("3").compareTo(repo.cantidadBase()));
    }

    @Test
    void devolucionesParcialesNoPierdenNiSobranCentavos() {
        // Arrange: se devuelve de a 1 unidad, acumulando lo ya devuelto
        VentaOriginal venta = venta(new LoteOriginal(100L, d("3")));
        var unaUnidad = List.of(new ItemDevueltoComando(LINEA, d("1"), true));
        Map<ClaveLinea, BigDecimal> devuelto = new HashMap<>();
        BigDecimal[] montos = new BigDecimal[3];

        // Act
        for (int i = 0; i < 3; i++) {
            montos[i] = CalculadorDevolucion.calcular(venta, unaUnidad, devuelto).monto();
            devuelto.merge(new ClaveLinea(LINEA, 100L), BigDecimal.ONE, BigDecimal::add);
        }

        // Assert: 3.33 + 3.34 + 3.33 = 10.00
        assertEquals(d("3.33"), montos[0]);
        assertEquals(d("3.34"), montos[1]);
        assertEquals(d("3.33"), montos[2]);
        assertEquals(d("10.00"), montos[0].add(montos[1]).add(montos[2]));
    }

    @Test
    void elRepartoRespetaElOrdenDeLosLotes() {
        // Arrange: la línea salió 1 u. del lote 100 y 2 u. del lote 200
        VentaOriginal venta = venta(new LoteOriginal(100L, d("1")), new LoteOriginal(200L, d("2")));
        var items = List.of(new ItemDevueltoComando(LINEA, d("2"), true));

        // Act
        CalculadorDevolucion.Calculo calculo = CalculadorDevolucion.calcular(venta, items, Map.of());

        // Assert: primero se completa el lote 100 y luego el 200
        List<LineaDevuelta> lineas = calculo.lineas();
        assertEquals(2, lineas.size());
        assertEquals(100L, lineas.get(0).loteId());
        assertEquals(0, d("1").compareTo(lineas.get(0).cantidadBase()));
        assertEquals(200L, lineas.get(1).loteId());
        assertEquals(0, d("1").compareTo(lineas.get(1).cantidadBase()));
        assertEquals(2, calculo.reposiciones().size());
        assertEquals(d("6.67"), calculo.monto());
    }

    @Test
    void noSePuedeDevolverMasDeLoVendido() {
        // Arrange: ya se devolvieron 2 de 3 unidades
        VentaOriginal venta = venta(new LoteOriginal(100L, d("3")));
        Map<ClaveLinea, BigDecimal> devuelto = Map.of(new ClaveLinea(LINEA, 100L), d("2"));
        var items = List.of(new ItemDevueltoComando(LINEA, d("2"), true));

        // Act + Assert
        assertThrows(CantidadExcedeLoVendidoException.class,
                () -> CalculadorDevolucion.calcular(venta, items, devuelto));
    }

    @Test
    void unaLineaQueNoEsDeLaVentaSeRechaza() {
        // Arrange
        VentaOriginal venta = venta(new LoteOriginal(100L, d("3")));
        var items = List.of(new ItemDevueltoComando(999L, d("1"), true));

        // Act + Assert
        assertThrows(DevolucionDatosInvalidosException.class,
                () -> CalculadorDevolucion.calcular(venta, items, Map.of()));
    }
}
