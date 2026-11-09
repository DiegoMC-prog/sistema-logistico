package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.ItemStock;
import com.tienda.backend.venta.application.model.ReposicionStock;

import java.util.List;

public interface StockVentaPort {
    /** Descuenta el stock repartiendo cada línea por FEFO. Lanza si no alcanza el stock de algún producto. */
    List<AsignacionStock> salir(Long depositoId, String motivo, List<ItemStock> items, Long ventaId);

    /** Devuelve stock a los mismos lotes de los que salió (anulación). */
    void reponer(Long depositoId, String observacion, List<ReposicionStock> items, Long ventaId);
}
