package com.tienda.backend.metodopago.application.port.out;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.metodopago.application.model.MetodoPagoFiltro;
import com.tienda.backend.metodopago.domain.model.MetodoPago;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MetodoPagoRepositoryPort {
    MetodoPago save(MetodoPago metodoPago);
    PageResult<MetodoPago> findAll(MetodoPagoFiltro filtro, int page, int size);
    Optional<MetodoPago> findById(Long id);
    List<MetodoPago> findAllById(Collection<Long> ids);
    Optional<MetodoPago> findByNombre(String nombre);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
