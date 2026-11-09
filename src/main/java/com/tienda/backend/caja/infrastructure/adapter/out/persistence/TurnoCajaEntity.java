package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos_caja")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class TurnoCajaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caja_id", nullable = false)
    private Long cajaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "monto_apertura", nullable = false, precision = 14, scale = 4)
    private BigDecimal montoApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_cierre_declarado", precision = 14, scale = 4)
    private BigDecimal montoCierreDeclarado;

    @Column(name = "monto_cierre_esperado", precision = 14, scale = 4)
    private BigDecimal montoCierreEsperado;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
