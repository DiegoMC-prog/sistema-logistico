package com.tienda.backend.metodopago.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.metodopago.application.model.MetodoPagoFiltro;
import com.tienda.backend.metodopago.application.port.out.MetodoPagoRepositoryPort;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MetodoPagoRepositoryAdapter implements MetodoPagoRepositoryPort {
    private final MetodoPagoJpaRepository metodoPagoJpaRepository;
    private final MetodoPagoPersistenceMapper mapper;

    @Override
    public MetodoPago save(MetodoPago metodoPago) {
        return mapper.toDomain(metodoPagoJpaRepository.save(mapper.toEntity(metodoPago)));
    }

    @Override
    public PageResult<MetodoPago> findAll(MetodoPagoFiltro filtro, int page, int size) {
        Page<MetodoPagoEntity> result = metodoPagoJpaRepository.findAll(
                MetodoPagoSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<MetodoPago> findById(Long id) {
        return metodoPagoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MetodoPago> findAllById(Collection<Long> ids) {
        return metodoPagoJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<MetodoPago> findByNombre(String nombre) {
        return metodoPagoJpaRepository.findByNombre(nombre).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        metodoPagoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return metodoPagoJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return metodoPagoJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}
