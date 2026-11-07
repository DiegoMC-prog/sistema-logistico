package com.tienda.backend.lote.application.port.in;

import com.tienda.backend.lote.application.model.LoteDetalle;

public interface ObtenerLoteUseCase {
    LoteDetalle obtener(Long id);
}
