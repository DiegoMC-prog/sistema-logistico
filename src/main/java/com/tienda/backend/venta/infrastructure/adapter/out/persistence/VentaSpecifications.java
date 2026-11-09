package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VentaSpecifications {

    private VentaSpecifications() {
    }

    public static Specification<VentaEntity> desde(VentaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.numero() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("numeroVenta"), filtro.numero()));
            }
            if (filtro.turnoId() != null) {
                predicados.add(cb.equal(root.get("turnoId"), filtro.turnoId()));
            }
            if (filtro.clienteId() != null) {
                predicados.add(cb.equal(root.get("clienteId"), filtro.clienteId()));
            }
            if (filtro.usuarioId() != null) {
                predicados.add(cb.equal(root.get("usuarioId"), filtro.usuarioId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fechaVenta"), filtro.desde().atStartOfDay()));
            }
            if (filtro.hasta() != null) {
                predicados.add(cb.lessThan(root.get("fechaVenta"), filtro.hasta().plusDays(1).atStartOfDay()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
