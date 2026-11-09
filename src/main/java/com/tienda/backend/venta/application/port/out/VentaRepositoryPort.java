package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface VentaRepositoryPort {
    /** Inserta la venta nueva o actualiza su cabecera; las líneas y pagos aún sin id se insertan. Devuelve la venta completa. */
    Venta save(Venta venta);
    Optional<Venta> findById(Long id);
    /** Lee la venta bloqueándola ({@code FOR UPDATE}): dos anulaciones simultáneas se serializan. */
    Optional<Venta> findByIdParaActualizar(Long id);
    Optional<Venta> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);
    /** Cabeceras de ventas, sin líneas ni pagos. */
    PageResult<Venta> findAll(VentaFiltro filtro, int page, int size);
}
