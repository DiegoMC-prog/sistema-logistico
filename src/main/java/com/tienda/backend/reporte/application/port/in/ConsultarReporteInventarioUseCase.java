package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.AgrupacionStock;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;

/** Reportes de inventario. Los que describen "el estado actual" devuelven {@code desde} y {@code hasta} nulos. */
public interface ConsultarReporteInventarioUseCase {
    ReporteResultado<FilasInventario.Stock> stockValorizado(AgrupacionStock agrupacion, Long depositoId,
                                                            Long categoriaId);

    ReporteResultado<FilasInventario.StockBajo> stockBajo(Long depositoId, Long categoriaId);

    ReporteResultado<FilasInventario.LoteVencimiento> lotesPorVencer(Integer dias, boolean incluirVencidos,
                                                                     Long depositoId);

    ReporteResultado<FilasInventario.Kardex> kardex(Long productoId, Long depositoId, RangoFechas rango);
}
