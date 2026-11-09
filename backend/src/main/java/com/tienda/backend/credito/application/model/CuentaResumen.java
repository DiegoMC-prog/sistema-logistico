package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CuentaResumen(Long id, Long ventaId, String numeroVenta, BigDecimal montoOriginal, BigDecimal saldo,
                            String estado, LocalDateTime fecha) {
}
