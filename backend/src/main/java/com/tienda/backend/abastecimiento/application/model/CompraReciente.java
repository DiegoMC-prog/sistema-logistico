package com.tienda.backend.abastecimiento.application.model;

import java.time.LocalDate;

/** Último pedido (no cancelado) de un producto a un proveedor: de aquí salen el proveedor y el costo adaptativo. */
public record CompraReciente(Long productoId, Long proveedorId, double costoUnitario, LocalDate fecha) {
}
