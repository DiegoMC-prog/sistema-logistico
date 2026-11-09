package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalle_lote")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VentaDetalleLoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_detalle_id", nullable = false)
    private Long ventaDetalleId;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "seleccion_manual", nullable = false)
    private boolean seleccionManual;

    @Column(name = "usuario_autorizado_id")
    private Long usuarioAutorizadoId;
}
