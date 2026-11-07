package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.infrastructure.adapter.out.persistence.CategoriaEntity;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@SQLDelete(sql = "UPDATE productos SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ProductoEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", referencedColumnName = "id", nullable = false)
    private CategoriaEntity categoria;

    @Builder.Default
    @Column(name = "stock_minimo", nullable = false, precision = 14, scale = 4)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "es_perecedero", nullable = false)
    private Boolean esPerecedero = Boolean.TRUE;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;
}
