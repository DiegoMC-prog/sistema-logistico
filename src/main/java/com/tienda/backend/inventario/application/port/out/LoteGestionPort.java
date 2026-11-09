package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.lote.application.model.CrearLoteComando;
import com.tienda.backend.lote.domain.model.Lote;

public interface LoteGestionPort {
    /** Crea un lote aplicando todas sus reglas (presentación base, fecha en perecederos, número automático). */
    Lote crearLote(CrearLoteComando comando);
}
