package com.tienda.backend.deposito.application.port.out;

import com.tienda.backend.deposito.application.model.DepositoFiltro;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface DepositoRepositoryPort {
    Deposito save(Deposito deposito);
    PageResult<Deposito> findAll(DepositoFiltro filtro, int page, int size);
    Optional<Deposito> findById(Long id);
    List<Deposito> findAllById(Collection<Long> ids);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
