package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.PresentacionVenta;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CatalogoVentaPort {
    Optional<PresentacionVenta> buscarPresentacion(Long presentacionId);
    Map<Long, String> nombresProducto(Collection<Long> productoIds);
}
