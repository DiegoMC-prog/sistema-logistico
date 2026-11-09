package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.out.ReporteUtilidadRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteUtilidadServiceTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static final RangoFechas RANGO = RangoFechas.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    // ------------------------------------------------------------------ utilidad

    private static FilasUtilidad.Producto producto(long id, String ingreso, String costo) {
        BigDecimal u = bd(ingreso).subtract(bd(costo));
        return new FilasUtilidad.Producto(id, "P" + id, "C", bd("1"), bd(ingreso), bd(costo), u, null);
    }

    @Test
    void laUtilidadPorProductoSumaYCalculaElMargenSobreElIngreso() {
        ReporteUtilidadRepositoryPort puerto = mock(ReporteUtilidadRepositoryPort.class);
        when(puerto.porProducto(eq(RANGO), any(), anyInt())).thenReturn(List.of(
                producto(1, "100.00", "60.00"), producto(2, "50.00", "45.00")));

        ReporteResultado<FilasUtilidad.Producto> r = new ReporteUtilidadService(puerto).porProducto(RANGO, null, null);

        assertEquals(bd("150.00"), r.totales().get("ingreso"));
        assertEquals(bd("105.00"), r.totales().get("costo"));
        assertEquals(bd("45.00"), r.totales().get("utilidad"));
        assertEquals(bd("30.00"), r.totales().get("margen"));   // 45 / 150
    }

    @Test
    void sinIngresoElMargenEsNuloYNoHayDivisionPorCero() {
        ReporteUtilidadRepositoryPort puerto = mock(ReporteUtilidadRepositoryPort.class);
        when(puerto.porProducto(eq(RANGO), any(), anyInt())).thenReturn(List.of());

        ReporteResultado<FilasUtilidad.Producto> r = new ReporteUtilidadService(puerto).porProducto(RANGO, null, null);

        assertNull(r.totales().get("margen"));
        assertEquals(bd("0.00"), r.totales().get("utilidad"));
    }

    @Test
    void laUtilidadNetaDescuentaLoDevueltoYRecuperaElCostoDeLoQueVolvioAlStock() {
        ReporteUtilidadRepositoryPort puerto = mock(ReporteUtilidadRepositoryPort.class);
        when(puerto.porPeriodo(RANGO, Agrupacion.DIA)).thenReturn(List.of(
                new FilasUtilidad.Periodo(LocalDate.of(2026, 11, 2), 3, bd("200.00"), bd("120.00"), bd("80.00"),
                        bd("40.00"), bd("30.00"), bd("18.00"), bd("68.00"))));

        ReporteResultado<FilasUtilidad.Periodo> r = new ReporteUtilidadService(puerto).porPeriodo(RANGO, null);

        assertEquals(3L, ((Number) r.totales().get("ventas")).longValue());
        assertEquals(bd("68.00"), r.totales().get("utilidadNeta"));
        assertEquals(bd("40.00"), r.totales().get("margen"));
    }

    @Test
    void laPerdidaPorPrecioBajoCostoSoloCuentaVentasVigentes() {
        ReporteUtilidadRepositoryPort puerto = mock(ReporteUtilidadRepositoryPort.class);
        LocalDateTime f = LocalDateTime.of(2026, 11, 3, 10, 0);
        when(puerto.preciosManuales(eq(RANGO), anyInt())).thenReturn(List.of(
                new FilasUtilidad.PrecioManual("V1", "COMPLETADA", f, "u", "A", bd("2"), bd("8"), bd("10"), bd("-2"),
                        true, bd("4.00"), bd("16.00"), "m"),
                new FilasUtilidad.PrecioManual("V2", "ANULADA", f, "u", "A", bd("5"), bd("8"), bd("10"), bd("-2"),
                        true, bd("10.00"), bd("40.00"), "m"),
                new FilasUtilidad.PrecioManual("V3", "COMPLETADA", f, "u", "B", bd("1"), bd("12"), bd("10"), bd("2"),
                        false, bd("0.00"), bd("12.00"), "m")));

        ReporteResultado<FilasUtilidad.PrecioManual> r = new ReporteUtilidadService(puerto).preciosManuales(RANGO, null);

        assertEquals(3, r.totales().get("lineas"));              // la anulada se lista para auditoria
        assertEquals(1L, ((Number) r.totales().get("bajoCosto")).longValue());
        assertEquals(bd("4.00"), r.totales().get("perdidaBajoCosto"));
        assertEquals(bd("28.00"), r.totales().get("importe"));
    }
}
