package com.tienda.backend.categoria.application.port.out;

import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.categoria.application.model.CategoriaFiltro;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface CategoriaRespositoryPort {
    Categoria save(Categoria categoria);
    PageResult<Categoria> findAll(CategoriaFiltro filtro, int page, int size);
    Optional<Categoria> findById(Long id);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
