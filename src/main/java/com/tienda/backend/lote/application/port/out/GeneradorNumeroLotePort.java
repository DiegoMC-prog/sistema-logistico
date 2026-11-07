package com.tienda.backend.lote.application.port.out;

public interface GeneradorNumeroLotePort {
    /** Siguiente número de lote automático, ya formateado y único. */
    String siguiente();
}
