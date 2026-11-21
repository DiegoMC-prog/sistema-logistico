package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record RegistrarAbonoComando(Long clienteId, Long metodoPagoId, BigDecimal monto, String referencia,
                                    /** R03: clave opcional; un reintento con la misma devuelve el mismo abono. */
                                    String idempotencyKey) {
}
