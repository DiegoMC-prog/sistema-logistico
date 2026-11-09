package com.tienda.backend.venta.application.model;

public record ClienteVenta(Long id, String nombre, Long tipoClienteId, boolean activo) {
}
