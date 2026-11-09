package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones_venta")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class DevolucionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "turno_id", nullable = false)
    private Long turnoId;

    @Column(name = "numero_devolucion", nullable = false, length = 30)
    private String numeroDevolucion;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "monto_reembolsado", nullable = false, precision = 14, scale = 4)
    private BigDecimal montoReembolsado;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "venta_cambio_id")
    private Long ventaCambioId;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "idempotency_hash", length = 64)
    private String idempotencyHash;
}
