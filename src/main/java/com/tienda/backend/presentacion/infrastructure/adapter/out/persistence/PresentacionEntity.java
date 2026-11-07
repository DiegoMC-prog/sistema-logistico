package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence.UnidadMedidaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "presentaciones_producto")
@SQLDelete(sql = "UPDATE presentaciones_producto SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class PresentacionEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_medida_id", referencedColumnName = "id", nullable = false)
    private UnidadMedidaEntity unidadMedida;

    @Column(name = "codigo_barras", nullable = false, length = 64)
    private String codigoBarras;

    @Column(name = "factor_conversion", nullable = false, precision = 12, scale = 4)
    private BigDecimal factorConversion;

    @Column(name = "es_unidad_base", nullable = false)
    private boolean esUnidadBase;
}
