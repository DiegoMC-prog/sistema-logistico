package com.tienda.backend.precio.application.port.out;

import com.tienda.backend.precio.application.model.PrecioFiltro;
import com.tienda.backend.precio.domain.model.Precio;
import com.tienda.backend.shared.application.model.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PrecioRepositoryPort {
    Precio save(Precio precio);
    Optional<Precio> findById(Long id);
    PageResult<Precio> findByPresentacion(Long presentacionId, PrecioFiltro filtro, int page, int size);
    /** Precios ACTIVOS de la presentación con cantidad mínima <= cantidad, generales y del tipo indicado. */
    List<Precio> findCandidatos(Long presentacionId, BigDecimal cantidad, Long tipoClienteId);
    void deleteById(Long id);
    boolean existsByClave(Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima);
    boolean existsByClaveAndIdNot(Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima, Long id);
}
