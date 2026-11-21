package com.tienda.backend.alerta.domain.model;

/** Qué condición disparó la alerta. */
public enum TipoAlerta {
    /** Lote con stock que vence dentro del plazo configurado (RF-13). */
    VENCIMIENTO,
    /** Lote ya vencido que todavía tiene stock (RF-13). */
    VENCIDO,
    /** Producto cuyo stock total quedó por debajo de su stock mínimo (RF-16). */
    STOCK_BAJO
}
