package com.tienda.backend.categoria.application.port.in;

import com.tienda.backend.categoria.domain.model.Categoria;

import java.util.List;

public interface ListarCategoriaUseCase {
    List<Categoria> listar();
}
