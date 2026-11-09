package com.tienda.backend.devolucion.application.service;

import com.tienda.backend.devolucion.application.model.DevolucionVista;
import com.tienda.backend.devolucion.application.model.MetodoPagoRef;
import com.tienda.backend.devolucion.application.model.VentaCambioRef;
import com.tienda.backend.devolucion.application.port.out.MetodoPagoDevolucionPort;
import com.tienda.backend.devolucion.application.port.out.VentaDevolucionPort;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Arma la devolución con nombres. Se usa después de confirmar la transacción para no alargar los bloqueos. */
@Component
@RequiredArgsConstructor
class DevolucionVistaFactory {
    private final MetodoPagoDevolucionPort metodoPagoDevolucionPort;
    private final VentaDevolucionPort ventaDevolucionPort;

    DevolucionVista construir(Devolucion d) {
        String metodo = d.getMetodoPagoId() == null ? null
                : metodoPagoDevolucionPort.buscarPorId(d.getMetodoPagoId()).map(MetodoPagoRef::nombre).orElse(null);
        VentaCambioRef cambio = d.getVentaCambioId() == null ? null
                : ventaDevolucionPort.resumenVenta(d.getVentaCambioId()).orElse(null);
        return new DevolucionVista(d, metodo, cambio);
    }
}
