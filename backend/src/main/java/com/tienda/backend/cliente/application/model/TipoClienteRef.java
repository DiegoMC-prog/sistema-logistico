package com.tienda.backend.cliente.application.model;

/** Datos mínimos de un tipo de cliente que necesita este módulo. */
public record TipoClienteRef(Long id, String nombre, boolean activo) {
}
