package com.tienda.backend.shared.domain.exception;

import java.math.BigDecimal;

/**
 * R08: la operación supera el monto umbral y hace falta la autorización de un administrador. Se responde 403 con
 * {@code code = AUTORIZACION_REQUERIDA}, el monto y el umbral: el frontend pide las credenciales y reintenta.
 */
public class AutorizacionRequeridaException extends DomainException {

    public static final String CODIGO = "AUTORIZACION_REQUERIDA";

    private final BigDecimal monto;
    private final BigDecimal umbral;

    public AutorizacionRequeridaException(String operacion, BigDecimal monto, BigDecimal umbral) {
        super("Para " + operacion + " por " + monto.toPlainString() + " (más de " + umbral.toPlainString()
              + ") hace falta la autorización de un administrador.");
        this.monto = monto;
        this.umbral = umbral;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getUmbral() {
        return umbral;
    }
}
