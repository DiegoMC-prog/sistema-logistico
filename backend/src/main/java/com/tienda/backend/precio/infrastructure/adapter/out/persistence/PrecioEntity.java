package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "precios_presentacion")
@SQLDelete(sql = "UPDATE precios_presentacion SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class PrecioEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "presentacion_id", nullable = false)
    private Long presentacionId;

    @Column(name = "tipo_cliente_id")
    private Long tipoClienteId;

    @Column(name = "cantidad_minima", nullable = false, precision = 14, scale = 4)
    private BigDecimal cantidadMinima;

    @Column(name = "precio", nullable = false, precision = 14, scale = 4)
    private BigDecimal precio;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
