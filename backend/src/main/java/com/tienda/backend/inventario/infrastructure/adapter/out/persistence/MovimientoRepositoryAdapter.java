package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import com.tienda.backend.inventario.application.port.out.MovimientoRepositoryPort;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MovimientoRepositoryAdapter implements MovimientoRepositoryPort {
    private final MovimientoJpaRepository movimientoJpaRepository;
    private final MovimientoDetalleJpaRepository detalleJpaRepository;
    private final MovimientoPersistenceMapper mapper;

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoEntity guardado = movimientoJpaRepository.save(mapper.toEntity(movimiento));
        List<MovimientoDetalleEntity> lineas = detalleJpaRepository.saveAll(movimiento.getDetalles().stream()
                .map(d -> mapper.toDetalleEntity(guardado.getId(), d))
                .toList());
        return mapper.toDomain(guardado, lineas);
    }

    @Override
    public Optional<Movimiento> findById(Long id) {
        return movimientoJpaRepository.findById(id)
                .map(entity -> mapper.toDomain(entity, detalleJpaRepository.findByMovimientoId(id)));
    }

    @Override
    public PageResult<Movimiento> findAll(MovimientoFiltro filtro, int page, int size) {
        Sort orden = Sort.by(Sort.Order.desc("fechaMovimiento"), Sort.Order.desc("id"));
        Page<MovimientoEntity> result = movimientoJpaRepository.findAll(
                MovimientoSpecifications.desde(filtro), PageRequest.of(page, size, orden));

        // Las líneas de toda la página en una sola consulta, en vez de una por movimiento.
        List<Long> ids = result.getContent().stream().map(MovimientoEntity::getId).toList();
        Map<Long, List<MovimientoDetalleEntity>> lineasPorMovimiento = ids.isEmpty()
                ? Map.of()
                : detalleJpaRepository.findByMovimientoIdIn(ids).stream()
                        .collect(Collectors.groupingBy(MovimientoDetalleEntity::getMovimientoId));

        return new PageResult<>(
                result.getContent().stream()
                        .map(e -> mapper.toDomain(e, lineasPorMovimiento.getOrDefault(e.getId(), List.of())))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
