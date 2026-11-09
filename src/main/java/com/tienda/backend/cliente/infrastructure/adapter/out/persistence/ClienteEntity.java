package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence.TipoClienteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "clientes")
@SQLDelete(sql = "UPDATE clientes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ClienteEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_cliente_id", nullable = false)
    private TipoClienteEntity tipoCliente;

    @Column(name = "documento", length = 30)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
