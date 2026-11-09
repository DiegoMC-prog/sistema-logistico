package com.tienda.backend.shared.infrastructure.adapter.out.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.util.Locale;

/** Utilidad para búsquedas "contiene" sin distinguir mayúsculas y sin que el usuario inyecte comodines. */
public final class LikeSupport {
    private static final char ESCAPE = '\\';

    private LikeSupport() {
    }

    public static Predicate containsIgnoreCase(CriteriaBuilder cb, Expression<String> campo, String texto) {
        String patron = "%" + escapar(texto.toLowerCase(Locale.ROOT)) + "%";
        return cb.like(cb.lower(campo), patron, ESCAPE);
    }

    private static String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
