package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    List<Categoria> buscarTodas();

    boolean existePorNombre(String nombre);

    void eliminar(Long id);

    boolean existePorId(Long id);
}
