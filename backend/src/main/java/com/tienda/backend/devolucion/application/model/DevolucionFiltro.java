package com.tienda.backend.devolucion.application.model;

import java.time.LocalDate;

/** Criterios opcionales; desde y hasta son fechas de la devolución, ambas inclusive. */
public record DevolucionFiltro(Long ventaId, Long usuarioId, Long turnoId, LocalDate desde, LocalDate hasta) {
}
