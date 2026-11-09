package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class MovimientoSpecifications {

    private MovimientoSpecifications() {
    }

    public static Specification<MovimientoEntity> desde(MovimientoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.tipo() != null) {
                predicados.add(cb.equal(root.get("tipoMovimiento"), filtro.tipo().name()));
            }
            if (filtro.depositoId() != null) {
                predicados.add(cb.or(
                        cb.equal(root.get("depositoOrigenId"), filtro.depositoId()),
                        cb.equal(root.get("depositoDestinoId"), filtro.depositoId())));
            }
            if (filtro.productoId() != null || filtro.loteId() != null) {
                Subquery<Long> lineas = query.subquery(Long.class);
                Root<MovimientoDetalleEntity> detalle = lineas.from(MovimientoDetalleEntity.class);
                List<Predicate> condiciones = new ArrayList<>();
                condiciones.add(cb.equal(detalle.get("movimientoId"), root.get("id")));
                if (filtro.productoId() != null) {
                    condiciones.add(cb.equal(detalle.get("productoId"), filtro.productoId()));
                }
                if (filtro.loteId() != null) {
                    condiciones.add(cb.equal(detalle.get("loteId"), filtro.loteId()));
                }
                lineas.select(detalle.<Long>get("id")).where(condiciones.toArray(new Predicate[0]));
                predicados.add(cb.exists(lineas));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(
                        root.<LocalDateTime>get("fechaMovimiento"), filtro.desde().atStartOfDay()));
            }
            if (filtro.hasta() != null) {
                // "hasta" es inclusivo: se compara contra el inicio del día siguiente.
                predicados.add(cb.lessThan(
                        root.<LocalDateTime>get("fechaMovimiento"), filtro.hasta().plusDays(1).atStartOfDay()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
