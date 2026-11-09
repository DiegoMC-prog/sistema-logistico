package com.tienda.backend.devolucion.application.port.out;

import com.tienda.backend.devolucion.application.model.ReposicionStock;

import java.util.List;

public interface StockDevolucionPort {
    /** Devuelve stock a los lotes de los que salió. */
    void reponer(Long depositoId, String observacion, List<ReposicionStock> items, Long devolucionId);
}
