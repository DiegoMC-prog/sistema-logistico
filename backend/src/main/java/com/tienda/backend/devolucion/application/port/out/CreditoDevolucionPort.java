package com.tienda.backend.devolucion.application.port.out;

import java.math.BigDecimal;

/** Deuda de crédito de la venta devuelta: lo devuelto se descuenta primero del saldo pendiente. */
public interface CreditoDevolucionPort {
    /** Descuenta del saldo de la venta hasta {@code monto} y devuelve lo descontado (cero si no hay deuda). */
    BigDecimal descontarDeuda(Long ventaId, BigDecimal monto);
}
