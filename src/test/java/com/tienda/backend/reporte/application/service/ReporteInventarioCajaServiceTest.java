package com.tienda.backend.reporte.application.service;

import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.out.ReporteCajaRepositoryPort;
import com.tienda.backend.reporte.application.port.out.ReporteInventarioRepositoryPort;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReporteInventarioCajaServiceTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private static final RangoFechas RANGO = RangoFechas.de(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    // ------------------------------------------------------------------ kardex

    private static FilasInventario.Kardex mov(long id, String tipo, String entrada, String salida) {
        return new FilasInventario.Kardex(id, LocalDateTime.of(2026, 11, 2, 10, (int) id), tipo, null, null, "u", null,
                null, bd(entrada), bd(salida), BigDecimal.ZERO);
    }

    @Test
    void elKardexAcumulaElSaldoApartirDelSaldoInicial() {
        ReporteInventarioRepositoryPort puerto = mock(ReporteInventarioRepositoryPort.class);
        when(puerto.nombreProducto(7L)).thenReturn(Optional.of("Arroz"));
        when(puerto.saldoKardexInicial(eq(7L), any(), eq(RANGO))).thenReturn(bd("100"));
        when(puerto.movimientosKardex(eq(7L), any(), eq(RANGO))).thenReturn(List.of(
                mov(1, "ENTRADA", "50", "0"), mov(2, "SALIDA", "0", "30"), mov(3, "TRASLADO", "0", "0"),
                mov(4, "SALIDA", "0", "12.5")));

        ReporteResultado<FilasInventario.Kardex> r = new ReporteInventarioService(puerto).kardex(7L, null, RANGO);

        assertEquals(List.of(bd("150"), bd("120"), bd("120"), bd("107.5")),
                r.filas().stream().map(FilasInventario.Kardex::saldo).toList());
        assertEquals(bd("100"), r.totales().get("saldoInicial"));
        assertEquals(bd("50"), r.totales().get("entradas"));
        assertEquals(bd("42.5"), r.totales().get("salidas"));
        assertEquals(bd("107.5"), r.totales().get("saldoFinal"));
    }

    @Test
    void elKardexExigeUnProductoQueExista() {
        ReporteInventarioRepositoryPort puerto = mock(ReporteInventarioRepositoryPort.class);
        ReporteInventarioService servicio = new ReporteInventarioService(puerto);

        assertThrows(ReporteParametrosInvalidosException.class, () -> servicio.kardex(null, null, RANGO));
        when(puerto.nombreProducto(99L)).thenReturn(Optional.empty());
        assertThrows(ProductoNotFoundException.class, () -> servicio.kardex(99L, null, RANGO));
    }

    @Test
    void lotesPorVencerValidaLaVentanaDeDias() {
        ReporteInventarioService servicio = new ReporteInventarioService(mock(ReporteInventarioRepositoryPort.class));
        assertThrows(ReporteParametrosInvalidosException.class, () -> servicio.lotesPorVencer(-1, true, null));
        assertThrows(ReporteParametrosInvalidosException.class, () -> servicio.lotesPorVencer(366, true, null));
    }

    // ------------------------------------------------------------------ caja

    private static FilasCaja.Cierre turno(long id, long cajaId, long usuarioId, String estado, String esperado,
                                          String declarado) {
        boolean cerrado = "CERRADO".equals(estado);
        return new FilasCaja.Cierre(id, cajaId, "Caja " + cajaId, usuarioId, "Cajero " + usuarioId, estado,
                LocalDateTime.of(2026, 11, 2, 8, 0), null, bd("100"), 3, bd("50"), bd("30"), bd("20"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, bd(esperado), cerrado ? bd(declarado) : null,
                cerrado ? bd(declarado).subtract(bd(esperado)) : null);
    }

    private ReporteCajaRepositoryPort cajaConTurnos() {
        ReporteCajaRepositoryPort puerto = mock(ReporteCajaRepositoryPort.class);
        when(puerto.cierres(eq(RANGO), any(), any(), any())).thenReturn(List.of(
                turno(1, 1, 10, "CERRADO", "200", "200"),      // cuadra
                turno(2, 1, 10, "CERRADO", "300", "295"),      // faltante 5
                turno(3, 2, 11, "CERRADO", "150", "153.5"),    // sobrante 3.5
                turno(4, 2, 11, "ABIERTO", "180", null)));     // abierto: no cuenta como cierre
        return puerto;
    }

    @Test
    void losCierresSoloSumanDiferenciasDeLosTurnosCerrados() {
        ReporteResultado<FilasCaja.Cierre> r = new ReporteCajaService(cajaConTurnos()).cierres(RANGO, null, null, null);

        assertEquals(4, r.totales().get("turnos"));
        assertEquals(1L, ((Number) r.totales().get("turnosAbiertos")).longValue());
        assertEquals(bd("-1.50"), r.totales().get("diferenciaCerrados"));
        assertEquals(2L, ((Number) r.totales().get("turnosConDiferencia")).longValue());
    }

    @Test
    void lasDiferenciasPorTurnoMuestranSoloLosQueNoCuadran() {
        ReporteResultado<FilasCaja.Cierre> r = new ReporteCajaService(cajaConTurnos()).diferenciasPorTurno(RANGO, null, null);

        assertEquals(List.of(2L, 3L), r.filas().stream().map(FilasCaja.Cierre::turnoId).toList());   // mayor diferencia primero
        assertEquals(bd("3.50"), r.totales().get("sobrante"));
        assertEquals(bd("-5.00"), r.totales().get("faltante"));
        assertEquals(bd("-1.50"), r.totales().get("neto"));
    }

    @Test
    void elResumenPorCajeroCuentaTodosLosTurnosCerradosYPonePrimeroElMayorFaltante() {
        ReporteResultado<FilasCaja.Diferencias> r = new ReporteCajaService(cajaConTurnos())
                .diferenciasResumen(RANGO, true, null, null);

        assertEquals(2, r.filas().size());
        FilasCaja.Diferencias primero = r.filas().get(0);
        assertEquals("Cajero 10", primero.nombre());
        assertEquals(2, primero.turnos());
        assertEquals(1, primero.turnosConDiferencia());
        assertEquals(bd("-5.00"), primero.neto());
        assertEquals(3L, ((Number) r.totales().get("turnos")).longValue());   // el turno abierto no entra
    }

    @Test
    void elEstadoDelFiltroDebeSerValido() {
        ReporteCajaService servicio = new ReporteCajaService(mock(ReporteCajaRepositoryPort.class));
        assertThrows(ReporteParametrosInvalidosException.class, () -> servicio.cierres(RANGO, null, null, "PENDIENTE"));
    }
}
