package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;

public interface ConsultarReporteComprasUseCase {
    ReporteResultado<FilasCompras.Proveedor> porProveedor(RangoFechas rango);

    /** Estado actual: pedidos enviados que faltan por recibir (los atrasados primero). */
    ReporteResultado<FilasCompras.Pendiente> pendientes(Long proveedorId, Long depositoId);

    /** Cómo cambió el costo de compra de un producto (el producto es obligatorio). */
    ReporteResultado<FilasCompras.CostoProducto> evolucionCosto(Long productoId, Long proveedorId, RangoFechas rango);
}
