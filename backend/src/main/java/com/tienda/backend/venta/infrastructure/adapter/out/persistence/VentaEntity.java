package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class VentaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_venta", nullable = false, length = 30)
    private String numeroVenta;

    @Column(name = "turno_id", nullable = false)
    private Long turnoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "tipo_cliente_id")
    private Long tipoClienteId;

    @Column(name = "deposito_id", nullable = false)
    private Long depositoId;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "descuento_total", nullable = false, precision = 14, scale = 4)
    private BigDecimal descuentoTotal;

    @Column(name = "total", nullable = false, precision = 14, scale = 4)
    private BigDecimal total;

    @Column(name = "vuelto", nullable = false, precision = 14, scale = 4)
    private BigDecimal vuelto;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "anulada_por", length = 150)
    private String anuladaPor;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "idempotency_hash", length = 64)
    private String idempotencyHash;
}
