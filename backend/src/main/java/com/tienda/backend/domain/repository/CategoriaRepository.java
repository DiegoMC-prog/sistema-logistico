package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    Page<Categoria> buscarTodas(Pageable pageable);

    boolean existePorNombre(String nombre);

    void eliminar(Long id);

    boolean existePorId(Long id);
}
