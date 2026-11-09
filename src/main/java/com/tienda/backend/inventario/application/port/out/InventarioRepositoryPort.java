package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.inventario.application.model.LoteDisponible;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import com.tienda.backend.inventario.application.model.ClaveStock;
import com.tienda.backend.inventario.application.model.StockDetalle;
import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.inventario.domain.model.Inventario;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface InventarioRepositoryPort {
    Optional<Inventario> findByDepositoIdAndLoteId(Long depositoId, Long loteId);

    /**
     * Bloquea (para modificar) las filas de stock indicadas y devuelve su estado actual. Las crea con stock cero si no
     * existen. Siempre las bloquea en el mismo orden global (lote y luego depósito) para que dos operaciones que toquen
     * las mismas filas en distinto orden no se bloqueen entre sí. Debe llamarse dentro de una transacción: los
     * bloqueos se liberan al confirmarla o revertirla.
     */
    Map<ClaveStock, Inventario> bloquear(Collection<ClaveStock> claves);

    Inventario save(Inventario inventario);

    /** Stock con nombres, ordenado por vencimiento (primero los que vencen antes) y luego por id. */
    PageResult<StockDetalle> findAll(StockFiltro filtro, int page, int size);

    boolean existeStockPorLote(Long loteId);

    boolean existeStockPorDeposito(Long depositoId);

    /**
     * Lotes con stock del depósito para esos productos, sin vencer a la fecha indicada, en orden FEFO: primero los que
     * vencen antes y al final los que no vencen. Es una lectura sin bloqueo para elegir candidatos; el stock real se
     * lee después de bloquear las filas.
     */
    List<LoteDisponible> findLotesDisponiblesFefo(Long depositoId, Collection<Long> productoIds, LocalDate hoy);
}
