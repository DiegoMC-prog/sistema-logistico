package com.tienda.backend.shared.application.model;

import java.math.BigDecimal;

/** Autorización concedida para una operación sobre el monto umbral; se registra en la bitácora al confirmarla. */
public record AutorizacionOtorgada(OperacionAutorizable operacion, BigDecimal monto, BigDecimal umbral,
                                   String solicitadoPor, String autorizadoPor) {
}
