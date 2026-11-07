package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import com.tienda.backend.presentacion.domain.model.Presentacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PresentacionRepositoryAdapter implements PresentacionRepositoryPort {
    private final PresentacionJpaRepository presentacionJpaRepository;
    private final PresentacionPersistenceMapper mapper;

    @Override
    public Presentacion save(Presentacion presentacion) {
        PresentacionEntity guardada = presentacionJpaRepository.save(mapper.toEntity(presentacion));
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Presentacion> findById(Long id) {
        return presentacionJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<PresentacionDetalle> findDetalleByProductoId(Long productoId) {
        return presentacionJpaRepository.findByProductoIdOrderByEsUnidadBaseDescIdAsc(productoId).stream()
                .map(mapper::toDetalle)
                .toList();
    }

    @Override
    public Optional<PresentacionDetalle> findDetalleByCodigoBarras(String codigoBarras) {
        return presentacionJpaRepository.findByCodigoBarras(codigoBarras).map(mapper::toDetalle);
    }

    @Override
    public boolean existsByCodigoBarras(String codigoBarras) {
        return presentacionJpaRepository.existsByCodigoBarras(codigoBarras);
    }

    @Override
    public boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id) {
        return presentacionJpaRepository.existsByCodigoBarrasAndIdNot(codigoBarras, id);
    }

    @Override
    public boolean existsBaseByProductoId(Long productoId) {
        return presentacionJpaRepository.existsByProductoIdAndEsUnidadBaseTrue(productoId);
    }

    @Override
    public long countByProductoId(Long productoId) {
        return presentacionJpaRepository.countByProductoId(productoId);
    }

    @Override
    public void deleteById(Long id) {
        presentacionJpaRepository.deleteById(id);
    }
}
