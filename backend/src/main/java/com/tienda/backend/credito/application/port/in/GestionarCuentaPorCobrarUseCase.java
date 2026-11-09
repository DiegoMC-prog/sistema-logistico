package com.tienda.backend.credito.application.port.in;

import java.math.BigDecimal;

/** Lo que otros módulos (ventas, devoluciones) necesitan de las cuentas por cobrar; siempre dentro de su transacción. */
public interface GestionarCuentaPorCobrarUseCase {
    /** Crea la cuenta de una venta a crédito validando que el cliente tenga crédito y no pase su límite. */
    void crearParaVenta(Long clienteId, Long ventaId, BigDecimal monto);

    /** Anula la cuenta de la venta (si la hay); falla si ya tiene abonos. */
    void anularDeVenta(Long ventaId);

    /** Descuenta del saldo pendiente de la venta hasta {@code monto}; devuelve lo descontado (cero si no hay deuda). */
    BigDecimal descontarPorDevolucion(Long ventaId, BigDecimal monto);
}
