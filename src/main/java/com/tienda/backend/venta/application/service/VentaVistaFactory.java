package com.tienda.backend.venta.application.service;

import com.tienda.backend.venta.application.model.ClienteVenta;
import com.tienda.backend.venta.application.model.MetodoPagoRef;
import com.tienda.backend.venta.application.model.VentaVista;
import com.tienda.backend.venta.application.port.out.CatalogoVentaPort;
import com.tienda.backend.venta.application.port.out.ClienteVentaPort;
import com.tienda.backend.venta.application.port.out.MetodoPagoVentaPort;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Arma la nota de venta con nombres. Se usa después de confirmar la transacción para no alargar los bloqueos. */
@Component
@RequiredArgsConstructor
class VentaVistaFactory {
    private final ClienteVentaPort clienteVentaPort;
    private final CatalogoVentaPort catalogoVentaPort;
    private final MetodoPagoVentaPort metodoPagoVentaPort;

    VentaVista construir(Venta venta) {
        String cliente = venta.getClienteId() == null ? null
                : clienteVentaPort.buscarPorId(venta.getClienteId()).map(ClienteVenta::nombre).orElse(null);
        Set<Long> productoIds = venta.getLineas().stream().map(LineaVenta::productoId).collect(Collectors.toSet());
        Set<Long> metodoIds = venta.getPagos().stream().map(PagoVenta::metodoPagoId).collect(Collectors.toSet());
        Map<Long, String> productos = productoIds.isEmpty() ? Map.of() : catalogoVentaPort.nombresProducto(productoIds);
        Map<Long, String> metodos = metodoIds.isEmpty() ? Map.of()
                : metodoPagoVentaPort.buscarPorIds(metodoIds).values().stream()
                        .collect(Collectors.toMap(MetodoPagoRef::id, MetodoPagoRef::nombre));
        return new VentaVista(venta, cliente, productos, metodos);
    }
}
