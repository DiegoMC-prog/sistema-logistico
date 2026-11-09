package com.tienda.backend.venta.application.port.in;

import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.venta.application.model.VentaVista;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarVentaUseCase {
    VentaVista obtener(Long ventaId);
    PageResult<Venta> listar(VentaFiltro filtro, int page, int size);
}
