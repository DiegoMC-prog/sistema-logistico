package com.tienda.backend.presentacion.application.port.out;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.domain.model.Presentacion;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

public interface PresentacionRepositoryPort {
    Presentacion save(Presentacion presentacion);
    Optional<Presentacion> findById(Long id);
    /** Presentacion base (con su unidad) de cada uno de los productos indicados. */
    List<PresentacionDetalle> findBasesDetalleByProductoIds(Collection<Long> productoIds);
    /** Presentaciones del producto con su unidad, la base primero. */
    List<PresentacionDetalle> findDetalleByProductoId(Long productoId);
    Optional<PresentacionDetalle> findDetalleByCodigoBarras(String codigoBarras);
    boolean existsByCodigoBarras(String codigoBarras);
    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);
    boolean existsBaseByProductoId(Long productoId);
    long countByProductoId(Long productoId);
    void deleteById(Long id);
}
