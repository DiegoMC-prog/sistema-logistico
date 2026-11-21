package com.tienda.backend.abastecimiento.domain.model;

import java.util.Collection;

/** Media y desviación estándar muestral de una serie. */
public record Estadistica(double media, double desviacion, int muestras) {

    public static final Estadistica VACIA = new Estadistica(0, 0, 0);

    public static Estadistica de(double[] serie) {
        int n = serie == null ? 0 : serie.length;
        if (n == 0) {
            return VACIA;
        }
        double suma = 0;
        for (double x : serie) {
            suma += x;
        }
        double media = suma / n;
        if (n == 1) {
            return new Estadistica(media, 0, 1);
        }
        double cuadrados = 0;
        for (double x : serie) {
            cuadrados += (x - media) * (x - media);
        }
        return new Estadistica(media, Math.sqrt(cuadrados / (n - 1)), n);
    }

    public static Estadistica de(Collection<Double> valores) {
        return de(valores == null ? null : valores.stream().mapToDouble(Double::doubleValue).toArray());
    }
}
