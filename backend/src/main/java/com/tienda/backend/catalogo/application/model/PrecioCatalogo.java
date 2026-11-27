package com.tienda.backend.catalogo.application.model;

import java.math.BigDecimal;

public record PrecioCatalogo(BigDecimal precio, BigDecimal recargo) {
}
