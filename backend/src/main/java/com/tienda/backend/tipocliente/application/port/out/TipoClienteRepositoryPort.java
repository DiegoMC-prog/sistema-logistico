package com.tienda.backend.tipocliente.application.port.out;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TipoClienteRepositoryPort {
    TipoCliente save(TipoCliente tipoCliente);
    PageResult<TipoCliente> findAll(TipoClienteFiltro filtro, int page, int size);
    Optional<TipoCliente> findById(Long id);
    List<TipoCliente> findAllById(Collection<Long> ids);
    Optional<TipoCliente> findByNombre(String nombre);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
