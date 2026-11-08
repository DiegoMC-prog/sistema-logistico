package com.tienda.backend.shared.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.config.security.UsuarioActual;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Auditoría común. Quién crea o modifica se toma del usuario autenticado de la petición;
 * fuera de una petición autenticada (semillas, tareas internas) se registra "SISTEMA".
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "created_by", nullable = false, length = 150, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 150)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        String actual = UsuarioActual.nombre();
        if (this.createdBy == null) {
            this.createdBy = actual;
        }
        if (this.updatedBy == null) {
            this.updatedBy = actual;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // En cada modificación quien la hace es el usuario actual, no el valor que traía la entidad.
        this.updatedBy = UsuarioActual.nombre();
    }
}
