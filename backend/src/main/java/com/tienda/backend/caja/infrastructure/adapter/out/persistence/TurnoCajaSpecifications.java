package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.TurnoFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TurnoCajaSpecifications {

    private TurnoCajaSpecifications() {
    }

    public static Specification<TurnoCajaEntity> desde(TurnoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.cajaId() != null) {
                predicados.add(cb.equal(root.get("cajaId"), filtro.cajaId()));
            }
            if (filtro.usuarioId() != null) {
                predicados.add(cb.equal(root.get("usuarioId"), filtro.usuarioId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fechaApertura"), filtro.desde().atStartOfDay()));
            }
            if (filtro.hasta() != null) {
                predicados.add(cb.lessThan(root.get("fechaApertura"), filtro.hasta().plusDays(1).atStartOfDay()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
