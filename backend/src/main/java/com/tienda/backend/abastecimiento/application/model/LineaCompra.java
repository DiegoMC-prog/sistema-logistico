package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

public record LineaCompra(Long productoId, BigDecimal cantidad, BigDecimal costoUnitario) {
}
