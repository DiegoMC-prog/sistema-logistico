package com.tienda.backend.abastecimiento.domain.service;

import com.tienda.backend.abastecimiento.domain.model.PuntoCosto;

import java.time.LocalDate;
import java.util.List;

/**
 * Variación del costo de un producto con un proveedor en los últimos N días (RF-35).
 * <p>
 * Compara el último costo con el costo vigente hace N días: el de la última compra hecha en esa fecha o antes. Si
 * todas las compras caen dentro de la ventana, se compara con la primera. No hay variación (null) si no hubo compras
 * en la ventana o si solo hay una compra.
 */
public final class VariacionCosto {

    private VariacionCosto() {
    }

    /**
     * @param puntos compras del par proveedor-producto ordenadas de la más antigua a la más reciente
     * @return variación como fracción (0.08 = +8 %), o null si no se puede calcular
     */
    public static Double calcular(List<PuntoCosto> puntos, LocalDate hoy, int dias) {
        if (puntos == null || puntos.size() < 2 || dias <= 0) {
            return null;
        }
        LocalDate corte = hoy.minusDays(dias);
        int ultimo = puntos.size() - 1;
        if (puntos.get(ultimo).fecha().isBefore(corte)) {
            return null;
        }
        int referencia = 0;
        for (int i = 0; i < ultimo; i++) {
            if (!puntos.get(i).fecha().isAfter(corte)) {
                referencia = i;
            }
        }
        double base = puntos.get(referencia).costo();
        if (base <= 0) {
            return null;
        }
        return (puntos.get(ultimo).costo() - base) / base;
    }
}
