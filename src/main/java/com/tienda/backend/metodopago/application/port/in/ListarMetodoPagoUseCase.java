package com.tienda.backend.metodopago.application.port.in;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.metodopago.application.model.MetodoPagoFiltro;
import com.tienda.backend.metodopago.domain.model.MetodoPago;

public interface ListarMetodoPagoUseCase {
    PageResult<MetodoPago> listar(MetodoPagoFiltro filtro, int page, int size);
}
