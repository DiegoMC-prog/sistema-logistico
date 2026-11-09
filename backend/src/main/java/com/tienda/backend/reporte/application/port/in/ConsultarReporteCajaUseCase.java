package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;

public interface ConsultarReporteCajaUseCase {
    /** Cierre de caja de cada turno del rango. {@code estado}: ABIERTO, CERRADO o nulo para ambos. */
    ReporteResultado<FilasCaja.Cierre> cierres(RangoFechas rango, Long cajaId, Long usuarioId, String estado);

    /** Turnos CERRADOS del rango que tuvieron sobrante o faltante. */
    ReporteResultado<FilasCaja.Cierre> diferenciasPorTurno(RangoFechas rango, Long cajaId, Long usuarioId);

    /** Resumen de sobrantes y faltantes de todos los turnos cerrados, por cajero o por caja. */
    ReporteResultado<FilasCaja.Diferencias> diferenciasResumen(RangoFechas rango, boolean porCajero, Long cajaId,
                                                               Long usuarioId);
}
