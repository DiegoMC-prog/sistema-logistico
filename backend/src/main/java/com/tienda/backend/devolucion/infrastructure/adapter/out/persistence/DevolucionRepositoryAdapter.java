package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.ClaveLinea;
import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import com.tienda.backend.devolucion.application.port.out.DevolucionRepositoryPort;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.devolucion.domain.model.LineaDevuelta;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DevolucionRepositoryAdapter implements DevolucionRepositoryPort {
    private final DevolucionJpaRepository devolucionJpaRepository;
    private final DevolucionDetalleJpaRepository detalleJpaRepository;
    private final DevolucionPersistenceMapper mapper;

    @Override
    public Devolucion save(Devolucion devolucion) {
        DevolucionEntity guardada = devolucionJpaRepository.save(mapper.toEntity(devolucion));
        // Las líneas sin id son nuevas; las que ya tienen id no cambian (una devolución no se edita)
        for (LineaDevuelta linea : devolucion.getLineas()) {
            if (linea.id() == null) {
                detalleJpaRepository.save(DevolucionDetalleEntity.builder().devolucionId(guardada.getId())
                        .ventaDetalleId(linea.ventaDetalleId()).loteId(linea.loteId()).cantidad(linea.cantidadBase())
                        .reingresaStock(linea.reingresaStock()).build());
            }
        }
        return cargar(guardada);
    }

    @Override
    public Optional<Devolucion> findById(Long id) {
        return devolucionJpaRepository.findById(id).map(this::cargar);
    }

    @Override
    public Optional<Devolucion> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey) {
        return devolucionJpaRepository.findByUsuarioIdAndIdempotencyKey(usuarioId, idempotencyKey).map(this::cargar);
    }

    @Override
    public PageResult<Devolucion> findAll(DevolucionFiltro filtro, int page, int size) {
        Page<DevolucionEntity> result = devolucionJpaRepository.findAll(DevolucionSpecifications.desde(filtro),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomainCabecera).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Map<ClaveLinea, BigDecimal> devueltoPorLinea(Long ventaId) {
        Map<ClaveLinea, BigDecimal> mapa = new HashMap<>();
        for (Object[] fila : detalleJpaRepository.devueltoPorLinea(ventaId)) {
            mapa.put(new ClaveLinea((Long) fila[0], (Long) fila[1]), (BigDecimal) fila[2]);
        }
        return mapa;
    }

    private Devolucion cargar(DevolucionEntity e) {
        return mapper.toDomain(e, detalleJpaRepository.findByDevolucionIdOrderByIdAsc(e.getId()));
    }
}
