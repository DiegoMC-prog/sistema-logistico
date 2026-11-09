package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DevolucionSpecifications {

    private DevolucionSpecifications() {
    }

    public static Specification<DevolucionEntity> desde(DevolucionFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.ventaId() != null) {
                predicados.add(cb.equal(root.get("ventaId"), filtro.ventaId()));
            }
            if (filtro.usuarioId() != null) {
                predicados.add(cb.equal(root.get("usuarioId"), filtro.usuarioId()));
            }
            if (filtro.turnoId() != null) {
                predicados.add(cb.equal(root.get("turnoId"), filtro.turnoId()));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fecha"), filtro.desde().atStartOfDay()));
            }
            if (filtro.hasta() != null) {
                predicados.add(cb.lessThan(root.get("fecha"), filtro.hasta().plusDays(1).atStartOfDay()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
