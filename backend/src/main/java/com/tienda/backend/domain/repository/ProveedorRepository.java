package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProveedorRepository {
    Proveedor guardar(Proveedor proveedor);
    Optional<Proveedor> buscarPorId(Long id);
    Page<Proveedor> listarTodos(Pageable pageable);
    Page<Proveedor> buscarPorNombre(String nombreEmpresa, Pageable pageable);
    boolean existePorNombreEmpresa(String nombreEmpresa);
    void eliminar(Long id);

    long contarTotal();
    long contarActivos();
    long contarInactivos();
}
