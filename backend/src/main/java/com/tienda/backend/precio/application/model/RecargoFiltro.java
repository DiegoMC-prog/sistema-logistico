package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.AlcanceRecargo;

/** Filtro de recargos; todos los campos son opcionales. {@code search} busca en el producto, la categoría y el motivo. */
public record RecargoFiltro(Boolean activo, AlcanceRecargo alcance, String search) {
}
