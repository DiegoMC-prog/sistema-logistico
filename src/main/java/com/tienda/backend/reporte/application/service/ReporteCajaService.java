package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteCajaUseCase;
import com.tienda.backend.reporte.application.port.out.ReporteCajaRepositoryPort;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReporteCajaService implements ConsultarReporteCajaUseCase {
    private static final String CERRADO = "CERRADO";

    private final ReporteCajaRepositoryPort reporteCajaRepositoryPort;

    @Override
    public ReporteResultado<FilasCaja.Cierre> cierres(RangoFechas rango, Long cajaId, Long usuarioId, String estado) {
        List<FilasCaja.Cierre> filas = reporteCajaRepositoryPort.cierres(rango, cajaId, usuarioId, normalizar(estado));
        List<FilasCaja.Cierre> cerrados = filas.stream().filter(f -> CERRADO.equals(f.estado())).toList();

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("turnos", filas.size());
        totales.put("turnosAbiertos", filas.size() - cerrados.size());
        totales.put("ventas", filas.stream().mapToLong(FilasCaja.Cierre::ventas).sum());
        totales.put("totalVentas", suma(filas, FilasCaja.Cierre::totalVentas));
        totales.put("ventasEfectivo", suma(filas, FilasCaja.Cierre::ventasEfectivo));
        totales.put("otrosMetodos", suma(filas, FilasCaja.Cierre::otrosMetodos));
        totales.put("ingresos", suma(filas, FilasCaja.Cierre::ingresos));
        totales.put("egresos", suma(filas, FilasCaja.Cierre::egresos));
        totales.put("reembolsosEfectivo", suma(filas, FilasCaja.Cierre::reembolsosEfectivo));
        // El esperado, el declarado y la diferencia solo tienen sentido en los turnos ya cerrados
        totales.put("efectivoEsperadoCerrados", suma(cerrados, FilasCaja.Cierre::efectivoEsperado));
        totales.put("efectivoDeclaradoCerrados", suma(cerrados, FilasCaja.Cierre::efectivoDeclarado));
        totales.put("diferenciaCerrados", suma(cerrados, FilasCaja.Cierre::diferencia));
        totales.put("turnosConDiferencia", cerrados.stream().filter(f -> f.diferencia().signum() != 0).count());
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    @Override
    public ReporteResultado<FilasCaja.Cierre> diferenciasPorTurno(RangoFechas rango, Long cajaId, Long usuarioId) {
        List<FilasCaja.Cierre> conDiferencia = cerrados(rango, cajaId, usuarioId)
                .stream().filter(f -> f.diferencia().signum() != 0)
                .sorted(Comparator.comparing(FilasCaja.Cierre::diferencia, Comparator.comparing(BigDecimal::abs)).reversed())
                .toList();
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("turnosConDiferencia", conDiferencia.size());
        totales.put("sobrante", suma(conDiferencia.stream().filter(f -> f.diferencia().signum() > 0).toList(),
                FilasCaja.Cierre::diferencia));
        totales.put("faltante", suma(conDiferencia.stream().filter(f -> f.diferencia().signum() < 0).toList(),
                FilasCaja.Cierre::diferencia));
        totales.put("neto", suma(conDiferencia, FilasCaja.Cierre::diferencia));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, conDiferencia);
    }

    @Override
    public ReporteResultado<FilasCaja.Diferencias> diferenciasResumen(RangoFechas rango, boolean porCajero, Long cajaId,
                                                                      Long usuarioId) {
        List<FilasCaja.Cierre> cerrados = cerrados(rango, cajaId, usuarioId);

        Map<Long, List<FilasCaja.Cierre>> grupos = new LinkedHashMap<>();
        Map<Long, String> nombres = new LinkedHashMap<>();
        for (FilasCaja.Cierre c : cerrados) {
            Long id = porCajero ? c.usuarioId() : c.cajaId();
            grupos.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(c);
            nombres.put(id, porCajero ? c.cajero() : c.caja());
        }
        List<FilasCaja.Diferencias> filas = grupos.entrySet().stream().map(e -> {
            List<FilasCaja.Cierre> turnos = e.getValue();
            BigDecimal sobrante = suma(turnos.stream().filter(t -> t.diferencia().signum() > 0).toList(),
                    FilasCaja.Cierre::diferencia);
            BigDecimal faltante = suma(turnos.stream().filter(t -> t.diferencia().signum() < 0).toList(),
                    FilasCaja.Cierre::diferencia);
            return new FilasCaja.Diferencias(e.getKey(), nombres.get(e.getKey()), turnos.size(),
                    turnos.stream().filter(t -> t.diferencia().signum() != 0).count(), sobrante, faltante,
                    sobrante.add(faltante));
        }).sorted(Comparator.comparing(FilasCaja.Diferencias::neto)).toList();   // mayores faltantes primero

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("turnos", cerrados.size());
        totales.put("turnosConDiferencia", filas.stream().mapToLong(FilasCaja.Diferencias::turnosConDiferencia).sum());
        totales.put("sobrante", suma(filas, FilasCaja.Diferencias::sobrante));
        totales.put("faltante", suma(filas, FilasCaja.Diferencias::faltante));
        totales.put("neto", suma(filas, FilasCaja.Diferencias::neto));
        return new ReporteResultado<>(rango.desde(), rango.hasta(), totales, filas);
    }

    /** Turnos cerrados del rango; solo ellos tienen declarado y diferencia. */
    private List<FilasCaja.Cierre> cerrados(RangoFechas rango, Long cajaId, Long usuarioId) {
        return reporteCajaRepositoryPort.cierres(rango, cajaId, usuarioId, CERRADO).stream()
                .filter(f -> CERRADO.equals(f.estado()) && f.diferencia() != null)
                .toList();
    }

    private static String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        String e = estado.trim().toUpperCase(Locale.ROOT);
        if (!e.equals("ABIERTO") && !e.equals(CERRADO)) {
            throw new ReporteParametrosInvalidosException("El estado debe ser ABIERTO o CERRADO");
        }
        return e;
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
