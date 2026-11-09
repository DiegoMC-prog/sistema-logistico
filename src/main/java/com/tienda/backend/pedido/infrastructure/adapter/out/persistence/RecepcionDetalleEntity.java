package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recepcion_pedido_detalle")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class RecepcionDetalleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recepcion_id", nullable = false)
    private Long recepcionId;

    @Column(name = "pedido_detalle_id", nullable = false)
    private Long pedidoDetalleId;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;
}
