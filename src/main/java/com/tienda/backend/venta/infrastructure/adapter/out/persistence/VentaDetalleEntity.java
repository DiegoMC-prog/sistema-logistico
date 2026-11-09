package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalle")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VentaDetalleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "presentacion_id", nullable = false)
    private Long presentacionId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "factor_conversion", nullable = false, precision = 12, scale = 4)
    private BigDecimal factorConversion;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", nullable = false, precision = 14, scale = 4)
    private BigDecimal descuento;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "es_precio_modificado", nullable = false)
    private boolean esPrecioModificado;

    @Column(name = "motivo_precio", length = 150)
    private String motivoPrecio;
}
