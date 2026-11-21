package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

public record PedidoGenerado(Long pedidoId, String numeroPedido, Long proveedorId, String proveedor, int lineas,
                             BigDecimal total) {
}
