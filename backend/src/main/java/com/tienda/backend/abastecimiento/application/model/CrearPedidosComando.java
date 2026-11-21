package com.tienda.backend.abastecimiento.application.model;

import com.tienda.backend.abastecimiento.domain.model.Escenario;

import java.math.BigDecimal;
import java.util.List;

/**
 * Convierte sugerencias en pedidos BORRADOR, uno por proveedor.
 *
 * @param escenario de qué escenario se toma la cantidad cuando el ítem no trae una propia
 */
public record CrearPedidosComando(Long depositoDestinoId, Escenario escenario, String observacion, List<Item> items) {
    /** @param cantidad cantidad a pedir (unidad base); null = la del escenario elegido */
    public record Item(Long sugerenciaId, BigDecimal cantidad) {
    }
}
