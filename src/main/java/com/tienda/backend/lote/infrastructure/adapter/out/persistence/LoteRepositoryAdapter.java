package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.lote.application.port.out.LoteRepositoryPort;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class LoteRepositoryAdapter implements LoteRepositoryPort {
    private final LoteJpaRepository loteJpaRepository;
    private final LotePersistenceMapper mapper;

    @Override
    public Lote save(Lote lote) {
        LoteEntity guardado = loteJpaRepository.save(mapper.toEntity(lote));
        return mapper.toDomain(guardado);
    }

    @Override
    public List<Lote> findAllById(Collection<Long> ids) {
        return loteJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Lote> findById(Long id) {
        return loteJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<LoteDetalle> findAll(LoteFiltro filtro, int page, int size) {
        Page<LoteEntity> result = loteJpaRepository.findAll(
                LoteSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDetalle).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        loteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByProductoIdAndNumeroLote(Long productoId, String numeroLote) {
        return loteJpaRepository.existsByProductoIdAndNumeroLote(productoId, numeroLote);
    }

    @Override
    public boolean existsByProductoIdAndNumeroLoteAndIdNot(Long productoId, String numeroLote, Long id) {
        return loteJpaRepository.existsByProductoIdAndNumeroLoteAndIdNot(productoId, numeroLote, id);
    }
}
