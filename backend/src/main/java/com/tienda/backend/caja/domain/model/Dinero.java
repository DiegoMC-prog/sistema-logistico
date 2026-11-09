package com.tienda.backend.caja.domain.model;

import com.tienda.backend.caja.domain.exception.CajaDatosInvalidosException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Normaliza montos al esquema DECIMAL(14,4) y valida el rango. */
final class Dinero {
    private static final BigDecimal MAXIMO = new BigDecimal("9999999999.9999");

    private Dinero() {
    }

    static BigDecimal normalizar(BigDecimal valor, String campo) {
        BigDecimal n = valor.setScale(4, RoundingMode.HALF_UP);
        if (n.compareTo(MAXIMO) > 0) {
            throw new CajaDatosInvalidosException(campo + " supera el valor máximo permitido");
        }
        return n;
    }

    static BigDecimal noNegativo(BigDecimal valor, String campo) {
        if (valor == null) {
            throw new CajaDatosInvalidosException(campo + " es obligatorio");
        }
        if (valor.signum() < 0) {
            throw new CajaDatosInvalidosException(campo + " no puede ser negativo");
        }
        return normalizar(valor, campo);
    }
}
