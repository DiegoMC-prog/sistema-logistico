package com.tienda.backend.presentacion.application.port.in;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.domain.model.Presentacion;

public interface CrearPresentacionUseCase {
    PresentacionDetalle create(Presentacion presentacion);
}
