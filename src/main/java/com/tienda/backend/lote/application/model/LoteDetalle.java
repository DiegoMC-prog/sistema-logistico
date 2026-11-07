package com.tienda.backend.lote.application.model;

import com.tienda.backend.lote.domain.model.Lote;

/** Modelo de lectura: el lote junto con el nombre de su producto. */
public record LoteDetalle(Lote lote, String productoNombre) {
}
