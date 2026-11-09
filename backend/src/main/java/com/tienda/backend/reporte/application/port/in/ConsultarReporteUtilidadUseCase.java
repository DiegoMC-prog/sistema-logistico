package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;

public interface ConsultarReporteUtilidadUseCase {
    ReporteResultado<FilasUtilidad.Producto> porProducto(RangoFechas rango, Long categoriaId, Integer limite);

    ReporteResultado<FilasUtilidad.Periodo> porPeriodo(RangoFechas rango, Agrupacion agrupacion);

    ReporteResultado<FilasUtilidad.PrecioManual> preciosManuales(RangoFechas rango, Integer limite);
}
