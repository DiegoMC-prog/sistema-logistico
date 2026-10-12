package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Deposito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface DepositoRepository {
    Deposito guardar(Deposito deposito);

    Optional<Deposito> buscarPorId(Long id);

    Page<Deposito> listarTodos(Pageable pageable);

    Page<Deposito> buscarPorNombre(String nombre, Pageable pageable);

    boolean existePorNombre(String nombre);

    void eliminar(Long id);

    long contarTotal();

    long contarActivos();

    long contarInactivos();
}
