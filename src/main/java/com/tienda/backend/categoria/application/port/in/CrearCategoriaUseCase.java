package com.tienda.backend.categoria.application.port.in;

import com.tienda.backend.categoria.domain.model.Categoria;

public interface CrearCategoriaUseCase {
    Categoria create(Categoria categoria);
}
