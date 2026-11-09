package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.producto.infrastructure.adapter.out.persistence.ProductoEntity;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lotes")
@SQLDelete(sql = "UPDATE lotes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class LoteEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", referencedColumnName = "id", nullable = false)
    private ProductoEntity producto;

    @Column(name = "numero_lote", nullable = false, length = 80)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "costo_unitario_compra", nullable = false, precision = 14, scale = 4)
    private BigDecimal costoUnitarioCompra;

    @Column(name = "porcentaje_descuento_vencimiento", nullable = false, precision = 14, scale = 4)
    private BigDecimal porcentajeDescuentoVencimiento;
}
