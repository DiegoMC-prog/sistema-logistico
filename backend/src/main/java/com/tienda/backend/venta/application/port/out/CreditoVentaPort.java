package com.tienda.backend.venta.application.port.out;

import java.math.BigDecimal;

/** Cuentas por cobrar vistas desde ventas: la parte de la venta pagada con CREDITO queda como deuda del cliente. */
public interface CreditoVentaPort {
    /** Crea la cuenta por cobrar de la venta; falla si el cliente no tiene crédito o excede su límite. */
    void crearCuenta(Long clienteId, Long ventaId, BigDecimal monto);

    /** Anula la cuenta de la venta, si la tiene. */
    void anularCuenta(Long ventaId);
}
