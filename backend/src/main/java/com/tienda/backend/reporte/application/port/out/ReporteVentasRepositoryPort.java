package com.tienda.backend.reporte.application.port.out;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.RangoFechas;

import java.util.List;

/** Consultas de agregación sobre ventas y devoluciones. Solo lectura. */
public interface ReporteVentasRepositoryPort {
    /** Una fila por período del rango (también los que no tuvieron ventas, en cero), en orden cronológico. */
    List<FilasVentas.Periodo> resumenPorPeriodo(RangoFechas rango, Agrupacion agrupacion);

    List<FilasVentas.Producto> porProducto(RangoFechas rango, Long categoriaId, int limite);

    List<FilasVentas.ProductoSinVentas> productosSinVentas(RangoFechas rango, Long categoriaId, int limite);

    List<FilasVentas.Origen> porCajero(RangoFechas rango);

    List<FilasVentas.Origen> porCaja(RangoFechas rango);

    List<FilasVentas.MetodoPago> porMetodoPago(RangoFechas rango);

    List<FilasVentas.Cliente> porCliente(RangoFechas rango, int limite);

    List<FilasVentas.Cliente> porTipoCliente(RangoFechas rango);

    /** Anulaciones, devoluciones y cambios del rango, la más reciente primero. */
    List<FilasVentas.Incidencia> incidencias(RangoFechas rango, int limite);
}
