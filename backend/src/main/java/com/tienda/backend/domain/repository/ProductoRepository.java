package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductoRepository {
    Producto guardar(Producto producto);
    Optional<Producto> buscarPorId(Long id);
    Page<Producto> listarTodos(Pageable pageable);
    Page<Producto> buscarPorFiltros(String busqueda, Long categoriaId, Boolean estado, Pageable pageable);
    boolean existePorCodigoBarras(String codigoBarras);
    boolean existePorCodigoBarrasYIdDistinto(String codigoBarras, Long id);
    boolean existePorCategoriaId(Long categoriaId);
    void eliminar(Long id);

    long contarTotal();
    long contarActivos();
    long contarInactivos();
}
