package com.tienda.backend.abastecimiento.application.port.out;

import com.tienda.backend.abastecimiento.application.model.LineaCompra;
import com.tienda.backend.abastecimiento.application.model.PedidoGenerado;

import java.util.List;

/** Crea pedidos a proveedor en BORRADOR a través del módulo de pedidos (mismas validaciones que la pantalla). */
public interface PedidoCompraPort {
    PedidoGenerado crearBorrador(Long proveedorId, Long depositoDestinoId, String observacion, List<LineaCompra> lineas);
}
