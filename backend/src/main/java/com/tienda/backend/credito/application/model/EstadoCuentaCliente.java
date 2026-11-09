package com.tienda.backend.credito.application.model;

import java.math.BigDecimal;
import java.util.List;

/** Deuda total del cliente y el detalle por venta, más sus abonos. */
public record EstadoCuentaCliente(Long clienteId, String clienteNombre, boolean creditoHabilitado,
                                  BigDecimal limiteCredito, BigDecimal deudaTotal, BigDecimal disponible,
                                  List<CuentaResumen> cuentas, List<AbonoResumen> abonos) {
}
