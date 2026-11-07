package com.tienda.backend.lote.application.port.out;

import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface LoteRepositoryPort {
    Lote save(Lote lote);
    Optional<Lote> findById(Long id);
    PageResult<LoteDetalle> findAll(LoteFiltro filtro, int page, int size);
    void deleteById(Long id);
    boolean existsByProductoIdAndNumeroLote(Long productoId, String numeroLote);
    boolean existsByProductoIdAndNumeroLoteAndIdNot(Long productoId, String numeroLote, Long id);
}
