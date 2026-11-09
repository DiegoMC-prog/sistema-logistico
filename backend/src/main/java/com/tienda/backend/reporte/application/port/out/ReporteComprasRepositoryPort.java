package com.tienda.backend.reporte.application.port.out;

import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;

import java.util.List;
import java.util.Optional;

/** Consultas de agregación sobre pedidos y recepciones a proveedores. Solo lectura. */
public interface ReporteComprasRepositoryPort {
    List<FilasCompras.Proveedor> porProveedor(RangoFechas rango);

    /** Pedidos ENVIADO o RECIBIDO_PARCIAL; los de fecha estimada más antigua primero. */
    List<FilasCompras.Pendiente> pendientes(java.time.LocalDate hoy, Long proveedorId, Long depositoId);

    Optional<String> nombreProducto(Long productoId);

    /** Recepciones del producto en el rango, en orden cronológico; {@code variacion} aún sin calcular. */
    List<FilasCompras.CostoProducto> comprasDeProducto(Long productoId, Long proveedorId, RangoFechas rango);
}
