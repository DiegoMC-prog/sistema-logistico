package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;

public interface ConsultarReporteVentasUseCase {
    ReporteResultado<FilasVentas.Periodo> resumen(RangoFechas rango, Agrupacion agrupacion);

    ReporteResultado<FilasVentas.Producto> porProducto(RangoFechas rango, Long categoriaId, Integer limite);

    ReporteResultado<FilasVentas.ProductoSinVentas> productosSinVentas(RangoFechas rango, Long categoriaId,
                                                                       Integer limite);

    ReporteResultado<FilasVentas.Origen> porCajero(RangoFechas rango);

    ReporteResultado<FilasVentas.Origen> porCaja(RangoFechas rango);

    ReporteResultado<FilasVentas.MetodoPago> porMetodoPago(RangoFechas rango);

    ReporteResultado<FilasVentas.Cliente> porCliente(RangoFechas rango, boolean agruparPorTipo, Integer limite);

    ReporteResultado<FilasVentas.Incidencia> incidencias(RangoFechas rango, Integer limite);
}
