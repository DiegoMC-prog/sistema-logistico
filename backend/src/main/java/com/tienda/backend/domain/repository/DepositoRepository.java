package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Deposito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DepositoRepository {

    Deposito guardar(Deposito deposito);

    Optional<Deposito> buscarPorId(Long id);

    Page<Deposito> listarTodas(Pageable pageable);

    boolean existePorNombre(String nombre);

    boolean existePorDescripcion(String descripcion);

    void eliminar(Long id);

    boolean existePorId(Long id);
}
