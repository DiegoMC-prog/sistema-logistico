package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

public record EscenarioResumen(BigDecimal eoq, BigDecimal stockSeguridad, BigDecimal puntoReorden, BigDecimal cantidad) {
}
