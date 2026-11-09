package com.tienda.backend.proveedor.infrastructure.adapter.out.persistence;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.domain.model.EstadoProveedor;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProveedorSpecifications {

    private ProveedorSpecifications() {
    }

    public static Specification<ProveedorEntity> desde(ProveedorFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(cb.or(
                        LikeSupport.containsIgnoreCase(cb, root.get("nombreEmpresa"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("nombreContacto"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("email"), filtro.search())));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("activo"), filtro.estado() == EstadoProveedor.ACTIVO));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
