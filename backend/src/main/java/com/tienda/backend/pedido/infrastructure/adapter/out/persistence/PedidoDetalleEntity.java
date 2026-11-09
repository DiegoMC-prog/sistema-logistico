package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_proveedor_detalle")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class PedidoDetalleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_pedida", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidadPedida;

    @Column(name = "cantidad_recibida", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidadRecibida;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoUnitario;
}
