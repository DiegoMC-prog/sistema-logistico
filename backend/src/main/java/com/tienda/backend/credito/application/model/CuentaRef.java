package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record CuentaRef(Long id, Long clienteId, BigDecimal montoOriginal, BigDecimal saldo, String estado) {
}
