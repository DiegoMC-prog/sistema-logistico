package com.tienda.backend.caja.application.port.out;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CajaRepositoryPort {
    Caja save(Caja caja);
    PageResult<Caja> findAll(CajaFiltro filtro, int page, int size);
    Optional<Caja> findById(Long id);
    List<Caja> findAllById(Collection<Long> ids);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
