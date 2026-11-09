package com.tienda.backend.lote.application.port.in;

import com.tienda.backend.lote.application.model.CrearLoteComando;
import com.tienda.backend.lote.application.model.LoteDetalle;

/**
 * Uso interno: un lote solo nace cuando entra mercadería, así que lo invoca la entrada de stock
 * (recepción, ingreso inicial o ajuste), que crea el lote, el movimiento y el inventario en una sola transacción.
 * No se expone por REST.
 */
public interface CrearLoteUseCase {
    LoteDetalle create(CrearLoteComando comando);
}
