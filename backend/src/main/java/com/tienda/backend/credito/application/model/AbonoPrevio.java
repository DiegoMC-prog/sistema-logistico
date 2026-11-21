package com.tienda.backend.credito.application.model;

/** Abono ya registrado con una clave de idempotencia: su id y la huella del contenido con que se registró. */
public record AbonoPrevio(Long abonoId, String huella) {
}
