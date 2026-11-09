package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Libro de movimientos: sin auditoría ni borrado, un movimiento nunca se modifica. */
@Entity
@Table(name = "movimientos_inventarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class MovimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_movimiento", nullable = false, length = 150)
    private String tipoMovimiento;

    @Column(name = "deposito_origen_id")
    private Long depositoOrigenId;

    @Column(name = "deposito_destino_id")
    private Long depositoDestinoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "referencia_tipo", length = 50)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;
}
