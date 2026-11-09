package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pedidos_proveedor")
// Con @Version, Hibernate enlaza también la versión en el borrado: el SQL debe esperar ese segundo parámetro.
@SQLDelete(sql = "UPDATE pedidos_proveedor SET deleted_at = NOW() WHERE id = ? AND version = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class PedidoEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", nullable = false, length = 30)
    private String numeroPedido;

    @Column(name = "proveedor_id", nullable = false)
    private Long proveedorId;

    @Column(name = "deposito_destino_id", nullable = false)
    private Long depositoDestinoId;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "total", nullable = false, precision = 14, scale = 4)
    private BigDecimal total;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
