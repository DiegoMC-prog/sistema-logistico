package com.tienda.backend.venta.application.model;

import com.tienda.backend.venta.domain.model.EstadoVenta;

import java.time.LocalDate;

/** Criterios opcionales; desde y hasta son fechas de venta, ambas inclusive. */
public record VentaFiltro(String numero, Long turnoId, Long clienteId, Long usuarioId, EstadoVenta estado,
                          LocalDate desde, LocalDate hasta) {
    public VentaFiltro {
        numero = (numero == null || numero.isBlank()) ? null : numero.trim();
    }
}
