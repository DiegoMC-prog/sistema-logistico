package com.tienda.backend.abastecimiento.domain.model;

/**
 * Escenarios de la novedad científica: se varían la demanda diaria y el lead time en una desviación estándar.
 * Pesimista = más demanda y más demora; optimista = menos demanda y menos demora.
 */
public enum Escenario {
    PESIMISTA(1),
    ESTANDAR(0),
    OPTIMISTA(-1);

    /** Cuántas desviaciones estándar se suman a la media de demanda y de lead time. */
    private final int desvios;

    Escenario(int desvios) {
        this.desvios = desvios;
    }

    public double ajustar(double media, double desviacion) {
        return Math.max(media + desvios * desviacion, 0);
    }
}
