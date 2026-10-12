package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoriaRepository {
    Categoria guardar(Categoria categoria);
    Optional<Categoria> buscarPorId(Long id);
    Page<Categoria> listarTodos(Pageable pageable);
    Page<Categoria> buscarPorNombre(String nombre, Pageable pageable);
    boolean existePorNombre(String nombre);
    void eliminar(Long id);

    long contarTotal();
    long contarCategoriasEnUso();
    long contarProductosPorCategoriaId(Long categoriaId);
    Map<Long, Long> contarProductosPorCategoriaIds(List<Long> categoriaIds);
}
