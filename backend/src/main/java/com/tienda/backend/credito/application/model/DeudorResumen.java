package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;

public record DeudorResumen(Long clienteId, String clienteNombre, String documento, BigDecimal limiteCredito,
                            BigDecimal deudaTotal, int cuentasPendientes) {
}
