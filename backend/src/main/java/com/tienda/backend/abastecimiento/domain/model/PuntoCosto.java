package com.tienda.backend.abastecimiento.domain.model;

import java.time.LocalDate;

/** Costo unitario pactado con un proveedor en un pedido. */
public record PuntoCosto(LocalDate fecha, double costo) {
}
