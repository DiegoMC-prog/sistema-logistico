package com.tienda.backend.caja.application.model;

import com.tienda.backend.caja.domain.model.TurnoCaja;

public record TurnoDetalle(TurnoCaja turno, String cajaNombre) {
}
