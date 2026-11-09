package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record AplicacionAbono(Long cuentaId, String numeroVenta, BigDecimal monto) {
}
