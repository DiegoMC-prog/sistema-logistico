package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<ProductoEntity> desde(ProductoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()));
            }
            if (filtro.categoriaId() != null) {
                predicados.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.esPerecedero() != null) {
                predicados.add(cb.equal(root.get("esPerecedero"), filtro.esPerecedero()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
