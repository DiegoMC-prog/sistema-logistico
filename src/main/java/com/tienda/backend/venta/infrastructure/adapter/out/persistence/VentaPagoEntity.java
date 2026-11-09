package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_pagos")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VentaPagoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;

    @Column(name = "monto", nullable = false, precision = 14, scale = 4)
    private BigDecimal monto;

    @Column(name = "referencia", length = 100)
    private String referencia;
}
