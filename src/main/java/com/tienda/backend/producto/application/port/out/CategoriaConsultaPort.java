package com.tienda.backend.producto.application.port.out;

import java.util.Optional;

public interface CategoriaConsultaPort {
    /** Nombre de la categoría, o vacío si no existe. */
    Optional<String> obtenerNombrePorId(Long categoriaId);
}
