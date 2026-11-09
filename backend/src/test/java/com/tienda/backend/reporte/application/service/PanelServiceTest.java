package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.Panel;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteCajaUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteComprasUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteInventarioUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteUtilidadUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteVentasUseCase;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PanelServiceTest {

    private ConsultarReporteVentasUseCase ventas;
    private ConsultarReporteCajaUseCase caja;
    private ConsultarReporteInventarioUseCase inventario;
    private ConsultarReporteComprasUseCase compras;
    private ConsultarReporteUtilidadUseCase utilidad;
    private UsuarioActualPort usuario;
    private PanelService servicio;

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @BeforeEach
    void preparar() {
        ventas = mock(ConsultarReporteVentasUseCase.class);
        caja = mock(ConsultarReporteCajaUseCase.class);
        inventario = mock(ConsultarReporteInventarioUseCase.class);
        compras = mock(ConsultarReporteComprasUseCase.class);
        utilidad = mock(ConsultarReporteUtilidadUseCase.class);
        usuario = mock(UsuarioActualPort.class);
        servicio = new PanelService(ventas, caja, inventario, compras, utilidad, usuario);
    }

    private void conPermisos(String... permisos) {
        Set<String> concedidos = Set.of(permisos);
        when(usuario.tienePermiso(any())).thenAnswer(i -> concedidos.contains(i.<String>getArgument(0)));
    }

    private static <F> ReporteResultado<F> resultado(List<F> filas) {
        return new ReporteResultado<>(null, null, Map.of(), filas);
    }

    // ------------------------------------------------------------------ permisos

    @Test
    void sinPermisosNoSeConsultaNingunReporteYElPanelVieneVacio() {
        conPermisos();

        Panel p = servicio.consultar();

        assertNotNull(p.generadoEn());
        assertNull(p.ventas());
        assertNull(p.caja());
        assertNull(p.inventario());
        assertNull(p.compras());
        assertNull(p.utilidad());
        verifyNoInteractions(ventas, caja, inventario, compras, utilidad);
    }

    @Test
    void cadaBloqueLlegaSoloConElPermisoDeSuReporte() {
        conPermisos("REPORTE_INVENTARIO");
        when(inventario.stockBajo(any(), any())).thenReturn(resultado(List.of()));
        when(inventario.lotesPorVencer(eq(7), anyBoolean(), any())).thenReturn(resultado(List.of()));

        Panel p = servicio.consultar();

        assertNotNull(p.inventario());
        assertNull(p.ventas());
        assertNull(p.utilidad());   // la utilidad no se filtra por tener inventario
        verifyNoInteractions(ventas, caja, compras, utilidad);
    }

    // ------------------------------------------------------------------ ventas

    private static FilasVentas.Periodo dia(LocalDate fecha, long ventas, String total, long anuladas) {
        return new FilasVentas.Periodo(fecha, ventas, bd(total), BigDecimal.ZERO.setScale(2), bd(total),
                ventas == 0 ? BigDecimal.ZERO.setScale(2) : bd(total).divide(BigDecimal.valueOf(ventas), 2, java.math.RoundingMode.HALF_UP),
                anuladas, BigDecimal.ZERO.setScale(2), 0, BigDecimal.ZERO.setScale(2), bd(total));
    }

    private void ventasDeSieteDias(String totalAyer, String totalHoy) {
        LocalDate hoy = LocalDate.now();
        List<FilasVentas.Periodo> dias = new ArrayList<>();
        for (int i = 6; i >= 2; i--) {
            dias.add(dia(hoy.minusDays(i), 1, "10.00", 0));
        }
        dias.add(dia(hoy.minusDays(1), totalAyer.equals("0.00") ? 0 : 2, totalAyer, 0));
        dias.add(dia(hoy, 3, totalHoy, 1));
        when(ventas.resumen(any(RangoFechas.class), eq(Agrupacion.DIA))).thenReturn(resultado(dias));
    }

    @Test
    void ventasCompararHoyConAyerYDevuelveLosUltimosSieteDias() {
        conPermisos("REPORTE_VENTAS");
        ventasDeSieteDias("200.00", "250.00");

        Panel.Ventas v = servicio.consultar().ventas();

        assertEquals(3, v.hoy().ventas());
        assertEquals(bd("250.00"), v.hoy().total());
        assertEquals(1, v.hoy().anuladas());
        assertEquals(bd("200.00"), v.ayer().total());
        assertEquals(bd("25.00"), v.variacionTotal());   // +25 % respecto de ayer
        assertEquals(7, v.ultimos7Dias().size());
        assertEquals(LocalDate.now(), v.ultimos7Dias().get(6).fecha());
    }

    @Test
    void sinVentasAyerLaVariacionEsNula() {
        conPermisos("REPORTE_VENTAS");
        ventasDeSieteDias("0.00", "250.00");

        assertNull(servicio.consultar().ventas().variacionTotal());
    }

    // ------------------------------------------------------------------ inventario

    @Test
    void inventarioCuentaCriticosVencidosYValorEnRiesgo() {
        conPermisos("REPORTE_INVENTARIO");
        List<FilasInventario.StockBajo> bajos = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {   // 7 productos bajo el mínimo; el último sin stock
            bajos.add(new FilasInventario.StockBajo((long) i, "P" + i, "C", i == 7 ? bd("0") : bd("5"), bd("10"),
                    i == 7 ? bd("10") : bd("5"), bd("0"), null));
        }
        when(inventario.stockBajo(any(), any())).thenReturn(resultado(bajos));
        LocalDate hoy = LocalDate.now();
        when(inventario.lotesPorVencer(eq(7), eq(true), any())).thenReturn(resultado(List.of(
                lote(1, hoy.minusDays(2), "40.00"), lote(2, hoy.plusDays(3), "60.00"), lote(3, hoy.plusDays(6), "25.50"))));

        Panel.Inventario i = servicio.consultar().inventario();

        assertEquals(7, i.productosBajoMinimo());
        assertEquals(1, i.sinStock());
        assertEquals(PanelService.CRITICOS, i.criticos().size());   // solo los 5 primeros
        assertEquals(2, i.lotesPorVencer7Dias());
        assertEquals(1, i.lotesVencidos());
        assertEquals(bd("125.50"), i.valorEnRiesgo());
    }

    private static FilasInventario.LoteVencimiento lote(long id, LocalDate vence, String valor) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), vence);
        return new FilasInventario.LoteVencimiento(id, "L" + id, 1L, "P", vence, dias, dias < 0 ? "VENCIDO" : "POR_VENCER",
                bd("1"), bd("1"), bd(valor), bd("0"));
    }

    // ------------------------------------------------------------------ caja y compras

    @Test
    void cajaListaLosTurnosAbiertos() {
        conPermisos("REPORTE_CAJA");
        FilasCaja.Cierre t = new FilasCaja.Cierre(4L, 2L, "Caja 2", 9L, "Ana", "ABIERTO", LocalDateTime.now(), null,
                bd("100.00"), 3, bd("90.00"), bd("60.00"), bd("30.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                bd("160.00"), null, null);
        when(caja.cierres(any(RangoFechas.class), any(), any(), eq("ABIERTO"))).thenReturn(resultado(List.of(t)));

        Panel.Caja c = servicio.consultar().caja();

        assertEquals(1, c.turnosAbiertos());
        assertEquals("Ana", c.turnos().get(0).cajero());
        assertEquals(bd("160.00"), c.turnos().get(0).efectivoEsperado());
    }

    private static FilasCompras.Pendiente pendiente(long id, Long atraso, String monto) {
        return new FilasCompras.Pendiente(id, "PP-" + id, "ENVIADO", 1L, "Prov", 1L, "Dep", LocalDate.now(), null, atraso,
                bd("100.00"), 1, bd("1"), bd(monto));
    }

    @Test
    void comprasPonePrimeroLosPedidosMasAtrasadosYLimitaATres() {
        conPermisos("REPORTE_COMPRAS");
        when(compras.pendientes(any(), any())).thenReturn(resultado(List.of(
                pendiente(1, 2L, "10.00"), pendiente(2, 9L, "20.00"), pendiente(3, 0L, "5.00"), pendiente(4, null, "5.00"),
                pendiente(5, 5L, "30.00"), pendiente(6, 1L, "15.00"))));

        Panel.Compras c = servicio.consultar().compras();

        assertEquals(6, c.pedidosPendientes());
        assertEquals(4, c.atrasados());
        assertEquals(bd("85.00"), c.montoPendiente());
        assertEquals(List.of(2L, 5L, 1L), c.masAtrasados().stream().map(Panel.PedidoAtrasado::pedidoId).toList());
    }

    // ------------------------------------------------------------------ utilidad

    @Test
    void utilidadCalculaHoyYLosUltimosSieteDias() {
        conPermisos("REPORTE_UTILIDAD");
        LocalDate hoy = LocalDate.now();
        List<FilasUtilidad.Periodo> dias = new ArrayList<>();
        for (int i = 6; i >= 1; i--) {
            dias.add(new FilasUtilidad.Periodo(hoy.minusDays(i), 1, bd("100.00"), bd("70.00"), bd("30.00"), bd("30.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO, bd("30.00")));
        }
        dias.add(new FilasUtilidad.Periodo(hoy, 2, bd("200.00"), bd("120.00"), bd("80.00"), bd("40.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, bd("80.00")));
        when(utilidad.porPeriodo(any(RangoFechas.class), eq(Agrupacion.DIA))).thenReturn(resultado(dias));

        Panel.Utilidad u = servicio.consultar().utilidad();

        assertEquals(bd("80.00"), u.utilidadHoy());
        assertEquals(bd("40.00"), u.margenHoy());
        assertEquals(bd("260.00"), u.utilidad7Dias());       // 6 x 30 + 80
        assertEquals(bd("32.50"), u.margen7Dias());          // 260 / 800
    }
}
