package com.tienda.backend.reporte.application.service;

import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.out.ReporteComprasRepositoryPort;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteComprasServiceTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static final RangoFechas RANGO = RangoFechas.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    // ------------------------------------------------------------------ compras

    private static FilasCompras.CostoProducto compra(int dia, String cantidad, String costo) {
        return new FilasCompras.CostoProducto(LocalDateTime.of(2026, 11, dia, 9, 0), "PP-" + dia, 1L, "Prov", "L" + dia,
                bd(cantidad), bd(costo), null);
    }

    @Test
    void laEvolucionDelCostoCalculaVariacionesYElPromedioPonderado() {
        ReporteComprasRepositoryPort puerto = mock(ReporteComprasRepositoryPort.class);
        when(puerto.nombreProducto(7L)).thenReturn(Optional.of("Arroz"));
        when(puerto.comprasDeProducto(eq(7L), any(), eq(RANGO))).thenReturn(List.of(
                compra(1, "10", "10.00"), compra(2, "30", "11.00"), compra(3, "10", "9.90")));

        ReporteResultado<FilasCompras.CostoProducto> r = new ReporteComprasService(puerto).evolucionCosto(7L, null, RANGO);

        assertNull(r.filas().get(0).variacion());
        assertEquals(bd("10.00"), r.filas().get(1).variacion());     // 10 -> 11
        assertEquals(bd("-10.00"), r.filas().get(2).variacion());    // 11 -> 9.90
        assertEquals(bd("10.58"), r.totales().get("costoPromedio")); // (10*10 + 30*11 + 10*9.90) / 50
    }

    @Test
    void laEvolucionDelCostoExigeUnProductoQueExista() {
        ReporteComprasRepositoryPort puerto = mock(ReporteComprasRepositoryPort.class);
        ReporteComprasService servicio = new ReporteComprasService(puerto);

        assertThrows(ReporteParametrosInvalidosException.class, () -> servicio.evolucionCosto(null, null, RANGO));
        when(puerto.nombreProducto(99L)).thenReturn(Optional.empty());
        assertThrows(ProductoNotFoundException.class, () -> servicio.evolucionCosto(99L, null, RANGO));
    }

    @Test
    void sinCompraAnteriorNoHayVariacionTotal() {
        ReporteComprasRepositoryPort puerto = mock(ReporteComprasRepositoryPort.class);
        when(puerto.nombreProducto(7L)).thenReturn(Optional.of("Arroz"));
        when(puerto.comprasDeProducto(eq(7L), any(), eq(RANGO))).thenReturn(List.of(compra(1, "10", "10.00")));

        ReporteResultado<FilasCompras.CostoProducto> r = new ReporteComprasService(puerto).evolucionCosto(7L, null, RANGO);

        assertNull(r.totales().get("variacionTotal"));
        assertEquals(bd("10.00"), r.totales().get("costoUltimo"));
    }

    @Test
    void losPendientesSeparanAtrasadosYPedidosSinFechaEstimada() {
        ReporteComprasRepositoryPort puerto = mock(ReporteComprasRepositoryPort.class);
        when(puerto.pendientes(any(), any(), any())).thenReturn(List.of(
                pendiente(1, 5L), pendiente(2, 0L), pendiente(3, null)));

        ReporteResultado<FilasCompras.Pendiente> r = new ReporteComprasService(puerto).pendientes(null, null);

        assertEquals(3, r.totales().get("pedidos"));
        assertEquals(1L, ((Number) r.totales().get("atrasados")).longValue());
        assertEquals(1L, ((Number) r.totales().get("sinFechaEstimada")).longValue());
        assertEquals(bd("300.00"), r.totales().get("montoPendiente"));
        assertNull(r.desde());   // es el estado actual, sin rango
    }

    private static FilasCompras.Pendiente pendiente(long id, Long atraso) {
        return new FilasCompras.Pendiente(id, "PP-" + id, "ENVIADO", 1L, "Prov", 1L, "Dep", LocalDate.of(2026, 11, 1),
                atraso == null ? null : LocalDate.of(2026, 11, 5), atraso, bd("200.00"), 2, bd("10"), bd("100.00"));
    }
}
