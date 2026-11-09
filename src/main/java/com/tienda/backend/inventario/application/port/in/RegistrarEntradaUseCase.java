package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.MovimientoDetallado;

public interface RegistrarEntradaUseCase {
    /** Ingresa mercadería a un depósito; si la línea trae un lote nuevo, lo crea en la misma transacción. */
    MovimientoDetallado registrarEntrada(EntradaComando comando);
}
