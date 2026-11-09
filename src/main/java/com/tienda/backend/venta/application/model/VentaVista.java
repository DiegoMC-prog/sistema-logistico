package com.tienda.backend.venta.application.model;

import com.tienda.backend.venta.domain.model.Venta;

import java.util.Map;

/** Venta con los nombres que necesita la nota de venta (cliente, productos y métodos de pago). */
public record VentaVista(Venta venta, String clienteNombre, Map<Long, String> productos, Map<Long, String> metodosPago) {
}
