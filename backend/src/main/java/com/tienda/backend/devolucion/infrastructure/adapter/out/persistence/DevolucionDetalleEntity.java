package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "devolucion_venta_detalle")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class DevolucionDetalleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "devolucion_id", nullable = false)
    private Long devolucionId;

    @Column(name = "venta_detalle_id", nullable = false)
    private Long ventaDetalleId;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "reingresa_stock", nullable = false)
    private boolean reingresaStock;
}
