package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_restablecimiento")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class TokenRestablecimientoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_at", nullable = false)
    private LocalDateTime expiraAt;

    @Column(name = "usado_at")
    private LocalDateTime usadoAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ip", length = 45)
    private String ip;

    @PrePersist
    void alCrear() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
