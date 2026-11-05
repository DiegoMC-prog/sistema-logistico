package com.tienda.backend.categoria.application.port.out;

import com.tienda.backend.categoria.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRespositoryPort {
    Categoria save(Categoria categoria);
    List<Categoria> findAll();
    Optional<Categoria> findById(Long id);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
