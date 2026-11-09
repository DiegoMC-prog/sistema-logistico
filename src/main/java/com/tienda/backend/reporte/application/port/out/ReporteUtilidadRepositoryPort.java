package com.tienda.backend.reporte.application.port.out;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;

import java.util.List;

/** Consultas de agregación sobre ingresos y costos de ventas. Solo lectura. */
public interface ReporteUtilidadRepositoryPort {
    /** Ordenado de mayor a menor utilidad. */
    List<FilasUtilidad.Producto> porProducto(RangoFechas rango, Long categoriaId, int limite);

    /** Una fila por período del rango (también los vacíos, en cero), en orden cronológico. */
    List<FilasUtilidad.Periodo> porPeriodo(RangoFechas rango, Agrupacion agrupacion);

    /** Líneas con precio manual del rango, la más reciente primero. */
    List<FilasUtilidad.PrecioManual> preciosManuales(RangoFechas rango, int limite);
}
