package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.application.model.PrecioFiltro;
import com.tienda.backend.precio.application.port.out.PrecioRepositoryPort;
import com.tienda.backend.precio.domain.model.Precio;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PrecioRepositoryAdapter implements PrecioRepositoryPort {
    private final PrecioJpaRepository precioJpaRepository;
    private final PrecioPersistenceMapper mapper;

    @Override
    public Precio save(Precio precio) {
        return mapper.toDomain(precioJpaRepository.save(mapper.toEntity(precio)));
    }

    @Override
    public Optional<Precio> findById(Long id) {
        return precioJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Precio> findByPresentacion(Long presentacionId, PrecioFiltro filtro, int page, int size) {
        Sort orden = Sort.by(Sort.Order.asc("tipoClienteId").nullsFirst(), Sort.Order.asc("cantidadMinima"), Sort.Order.asc("id"));
        Page<PrecioEntity> result = precioJpaRepository.findAll(
                PrecioSpecifications.desde(presentacionId, filtro), PageRequest.of(page, size, orden));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public List<Precio> findCandidatos(Long presentacionId, BigDecimal cantidad, Long tipoClienteId) {
        return precioJpaRepository.findCandidatos(presentacionId, cantidad, tipoClienteId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        precioJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByClave(Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima) {
        return precioJpaRepository.existsByPresentacionIdAndTipoClienteIdAndCantidadMinima(
                presentacionId, tipoClienteId, cantidadMinima);
    }

    @Override
    public boolean existsByClaveAndIdNot(Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima, Long id) {
        return precioJpaRepository.existsByPresentacionIdAndTipoClienteIdAndCantidadMinimaAndIdNot(
                presentacionId, tipoClienteId, cantidadMinima, id);
    }
}
