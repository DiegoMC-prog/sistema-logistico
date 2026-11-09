package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Tabla de unión usuario-rol (sin borrado lógico en el esquema; guarda quién y cuándo asignó). */
@Entity
@Table(name = "usuario_rol")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class UsuarioRolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "assigned_by", nullable = false, length = 150)
    private String assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
