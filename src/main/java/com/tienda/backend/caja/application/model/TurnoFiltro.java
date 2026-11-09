package com.tienda.backend.caja.application.model;

import com.tienda.backend.caja.domain.model.EstadoTurno;

import java.time.LocalDate;

/** Criterios opcionales para listar turnos; desde y hasta son fechas de apertura, ambas inclusive. */
public record TurnoFiltro(Long cajaId, Long usuarioId, EstadoTurno estado, LocalDate desde, LocalDate hasta) {
}
