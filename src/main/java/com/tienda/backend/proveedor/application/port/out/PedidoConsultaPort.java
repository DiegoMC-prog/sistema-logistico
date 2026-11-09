package com.tienda.backend.proveedor.application.port.out;

public interface PedidoConsultaPort {
    /** true si el proveedor tiene algún pedido registrado. */
    boolean existenPedidosDelProveedor(Long proveedorId);
}
