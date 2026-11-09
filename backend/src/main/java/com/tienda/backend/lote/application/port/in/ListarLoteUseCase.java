package com.tienda.backend.lote.application.port.in;

import com.tienda.backend.lote.application.model.LoteDetalle;
import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarLoteUseCase {
    PageResult<LoteDetalle> listar(LoteFiltro filtro, int page, int size);
}
