package com.tienda.backend.abastecimiento.domain.model;

/**
 * Resultado de un escenario. Cantidades en unidad base.
 *
 * @param eoq           lote económico de pedido √(2·D·S / H)
 * @param puntoReorden  demanda durante el lead time + stock de seguridad
 * @param cantidad      cantidad sugerida a pedir (0 si no hace falta), ya limitada por vencimiento si corresponde
 * @param limitadaPorVencimiento true si la cantidad se recortó para no comprar más de lo que se vende antes de vencer
 */
public record ResultadoEscenario(
        Escenario escenario,
        double demandaDiaria,
        double leadTimeDias,
        double eoq,
        double stockSeguridad,
        double puntoReorden,
        double cantidad,
        boolean limitadaPorVencimiento
) {
    public static ResultadoEscenario vacio(Escenario escenario) {
        return new ResultadoEscenario(escenario, 0, 0, 0, 0, 0, 0, false);
    }
}
