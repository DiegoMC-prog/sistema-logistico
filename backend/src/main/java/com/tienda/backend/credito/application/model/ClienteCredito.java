package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record ClienteCredito(Long id, String nombre, boolean activo, boolean creditoHabilitado, BigDecimal limiteCredito) {
}
