package com.tienda.backend.reporte.application.port.out;

import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.RangoFechas;

import java.util.List;

/** Consultas de agregación sobre turnos de caja. Solo lectura. */
public interface ReporteCajaRepositoryPort {
    /**
     * Turnos abiertos en el rango (por fecha de apertura) con sus cobros, ingresos, egresos y reembolsos,
     * del más reciente al más antiguo. Los filtros nulos no se aplican; {@code estado} es ABIERTO o CERRADO.
     */
    List<FilasCaja.Cierre> cierres(RangoFechas rango, Long cajaId, Long usuarioId, String estado);
}
