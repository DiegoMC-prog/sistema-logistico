package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.lote.infrastructure.adapter.out.persistence.LoteEntity;
import com.tienda.backend.producto.infrastructure.adapter.out.persistence.ProductoEntity;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class InventarioSpecifications {

    private InventarioSpecifications() {
    }

    public static Specification<InventarioEntity> desde(StockFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            Join<InventarioEntity, LoteEntity> lote = root.join("lote");
            Join<LoteEntity, ProductoEntity> producto = lote.join("producto");

            if (filtro.depositoId() != null) {
                predicados.add(cb.equal(root.get("deposito").get("id"), filtro.depositoId()));
            }
            if (filtro.productoId() != null) {
                predicados.add(cb.equal(producto.get("id"), filtro.productoId()));
            }
            if (filtro.loteId() != null) {
                predicados.add(cb.equal(lote.get("id"), filtro.loteId()));
            }
            if (filtro.search() != null) {
                predicados.add(cb.or(
                        LikeSupport.containsIgnoreCase(cb, producto.get("nombre"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, lote.get("numeroLote"), filtro.search())));
            }
            if (Boolean.TRUE.equals(filtro.conStock())) {
                predicados.add(cb.greaterThan(root.<BigDecimal>get("stockActual"), BigDecimal.ZERO));
            } else if (Boolean.FALSE.equals(filtro.conStock())) {
                predicados.add(cb.equal(root.<BigDecimal>get("stockActual"), BigDecimal.ZERO));
            }
            if (filtro.venceHasta() != null) {
                predicados.add(cb.lessThanOrEqualTo(lote.<LocalDate>get("fechaVencimiento"), filtro.venceHasta()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
