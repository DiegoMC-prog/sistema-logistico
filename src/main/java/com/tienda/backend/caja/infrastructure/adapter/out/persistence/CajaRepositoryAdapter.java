package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.caja.application.port.out.CajaRepositoryPort;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.shared.application.model.PageResult;
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
public class CajaRepositoryAdapter implements CajaRepositoryPort {
    private final CajaJpaRepository cajaJpaRepository;
    private final CajaPersistenceMapper mapper;

    @Override
    public Caja save(Caja caja) {
        return mapper.toDomain(cajaJpaRepository.save(mapper.toEntity(caja)));
    }

    @Override
    public PageResult<Caja> findAll(CajaFiltro filtro, int page, int size) {
        Page<CajaEntity> result = cajaJpaRepository.findAll(CajaSpecifications.desde(filtro),
                PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Optional<Caja> findById(Long id) {
        return cajaJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Caja> findAllById(Collection<Long> ids) {
        return cajaJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        cajaJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return cajaJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return cajaJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}
