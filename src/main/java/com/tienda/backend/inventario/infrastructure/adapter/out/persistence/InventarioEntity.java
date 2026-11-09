package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.infrastructure.adapter.out.persistence.DepositoEntity;
import com.tienda.backend.lote.infrastructure.adapter.out.persistence.LoteEntity;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Stock de un lote en un depósito. {@code version} activa el bloqueo optimista: si dos operaciones modifican la misma
 * fila a la vez, la segunda falla en vez de pisar el resultado de la primera.
 */
@Entity
@Table(name = "inventarios")
@SQLRestriction("deleted_at IS NULL")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class InventarioEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deposito_id", referencedColumnName = "id", nullable = false)
    private DepositoEntity deposito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", referencedColumnName = "id", nullable = false)
    private LoteEntity lote;

    @Column(name = "stock_actual", nullable = false, precision = 14, scale = 4)
    private BigDecimal stockActual;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
