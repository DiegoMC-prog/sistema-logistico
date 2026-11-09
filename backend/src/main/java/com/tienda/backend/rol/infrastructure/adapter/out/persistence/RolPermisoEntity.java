package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

/** Tabla de unión rol-permiso (sin auditoría ni borrado lógico en el esquema). */
@Entity
@Table(name = "rol_permiso")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class RolPermisoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    @Column(name = "permiso_id", nullable = false)
    private Long permisoId;
}
