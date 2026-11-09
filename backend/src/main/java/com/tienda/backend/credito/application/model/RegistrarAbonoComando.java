package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record RegistrarAbonoComando(Long clienteId, Long metodoPagoId, BigDecimal monto, String referencia) {
}
