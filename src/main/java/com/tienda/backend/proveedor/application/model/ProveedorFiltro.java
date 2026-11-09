package com.tienda.backend.proveedor.application.model;

import com.tienda.backend.proveedor.domain.model.EstadoProveedor;

/** Criterios opcionales de búsqueda de proveedores; un campo nulo significa "sin filtrar por él". */
public record ProveedorFiltro(String search, EstadoProveedor estado) {
    public ProveedorFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}
