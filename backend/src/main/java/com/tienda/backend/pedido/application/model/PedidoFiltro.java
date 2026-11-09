package com.tienda.backend.pedido.application.model;

import com.tienda.backend.pedido.domain.model.EstadoPedido;

import java.time.LocalDate;

/** Criterios opcionales de búsqueda de pedidos; un campo nulo significa "sin filtrar por él". */
public record PedidoFiltro(String numero, Long proveedorId, Long depositoId, EstadoPedido estado,
                           LocalDate desde, LocalDate hasta) {
    public PedidoFiltro {
        numero = (numero == null || numero.isBlank()) ? null : numero.trim();
    }
}
