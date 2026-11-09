package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;

public record ResultadoVentaCambio(VentaCambioRef venta, BigDecimal creditoAplicado) {
}
