package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AbonoResumen(Long id, String numeroAbono, Long clienteId, String clienteNombre, Long turnoId,
                           String usuario, String metodoPago, BigDecimal monto, String referencia,
                           LocalDateTime fecha, List<AplicacionAbono> aplicaciones) {
}
