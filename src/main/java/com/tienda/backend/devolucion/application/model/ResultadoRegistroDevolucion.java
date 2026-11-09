package com.tienda.backend.devolucion.application.model;

/** {@code creada} es false si era un reintento con la misma clave de idempotencia y se devolvió la devolución existente. */
public record ResultadoRegistroDevolucion(DevolucionVista vista, boolean creada) {
}
