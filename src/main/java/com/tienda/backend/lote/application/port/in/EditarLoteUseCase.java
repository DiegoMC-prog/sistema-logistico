package com.tienda.backend.lote.application.port.in;

import com.tienda.backend.lote.application.model.EditarLoteComando;
import com.tienda.backend.lote.application.model.LoteDetalle;

public interface EditarLoteUseCase {
    LoteDetalle editar(Long id, EditarLoteComando comando);
}
