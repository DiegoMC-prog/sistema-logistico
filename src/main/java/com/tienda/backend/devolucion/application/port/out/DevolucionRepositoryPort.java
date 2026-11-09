package com.tienda.backend.devolucion.application.port.out;

import com.tienda.backend.devolucion.application.model.ClaveLinea;
import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.shared.application.model.PageResult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface DevolucionRepositoryPort {
    /** Inserta la devolución con sus líneas, o actualiza su cabecera si ya tiene id. Devuelve la devolución completa. */
    Devolucion save(Devolucion devolucion);
    Optional<Devolucion> findById(Long id);
    Optional<Devolucion> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);
    /** Cabeceras, sin líneas. */
    PageResult<Devolucion> findAll(DevolucionFiltro filtro, int page, int size);
    /** Cuánto (en unidad base) ya se devolvió de cada línea y lote de la venta. */
    Map<ClaveLinea, BigDecimal> devueltoPorLinea(Long ventaId);
}
