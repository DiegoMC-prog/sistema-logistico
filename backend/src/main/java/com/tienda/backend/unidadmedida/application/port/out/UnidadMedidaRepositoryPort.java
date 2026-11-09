package com.tienda.backend.unidadmedida.application.port.out;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

import java.util.Optional;

public interface UnidadMedidaRepositoryPort {
    UnidadMedida save(UnidadMedida unidadMedida);
    PageResult<UnidadMedida> findAll(UnidadMedidaFiltro filtro, int page, int size);
    Optional<UnidadMedida> findById(Long id);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    boolean existsByAbreviatura(String abreviatura);
    boolean existsByAbreviaturaAndIdNot(String abreviatura, Long id);
}
