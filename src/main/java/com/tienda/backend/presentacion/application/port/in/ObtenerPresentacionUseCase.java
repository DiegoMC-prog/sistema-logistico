package com.tienda.backend.presentacion.application.port.in;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;

public interface ObtenerPresentacionUseCase {
    PresentacionDetalle obtener(Long id);
    PresentacionDetalle obtenerPorCodigoBarras(String codigoBarras);
}
