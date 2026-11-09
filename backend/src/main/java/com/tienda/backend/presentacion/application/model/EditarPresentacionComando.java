package com.tienda.backend.presentacion.application.model;

import java.math.BigDecimal;

/** Datos editables de una presentación; la condición de base y el producto no se pueden cambiar. */
public record EditarPresentacionComando(Long unidadMedidaId, String codigoBarras, BigDecimal factorConversion) {
}
