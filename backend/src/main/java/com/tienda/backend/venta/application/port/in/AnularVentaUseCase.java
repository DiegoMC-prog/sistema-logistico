package com.tienda.backend.venta.application.port.in;

import com.tienda.backend.venta.application.model.VentaVista;

public interface AnularVentaUseCase {
    VentaVista anular(Long ventaId, String motivo);
}
