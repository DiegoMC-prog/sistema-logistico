package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Deposito;

import java.util.List;
import java.util.Optional;

public interface DepositoRepository {

    Deposito guardar(Deposito deposito);

    Optional<Deposito> buscarPorId(Long id);

    List<Deposito> listarTodas();

    boolean existePorNombre(String nombre);

    boolean existePorDescripcion(String descripcion);

    void eliminar(Long id);

    boolean existePorId(Long id);
}
