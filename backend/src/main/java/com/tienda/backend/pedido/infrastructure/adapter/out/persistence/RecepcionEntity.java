package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recepciones_pedido")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class RecepcionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDateTime fechaRecepcion;

    @Column(name = "numero_guia", length = 80)
    private String numeroGuia;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;
}
