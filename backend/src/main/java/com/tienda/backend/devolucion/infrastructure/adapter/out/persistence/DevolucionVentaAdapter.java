package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.ItemNuevoComando;
import com.tienda.backend.devolucion.application.model.PagoCambioComando;
import com.tienda.backend.devolucion.application.model.ResultadoVentaCambio;
import com.tienda.backend.devolucion.application.model.VentaCambioRef;
import com.tienda.backend.devolucion.application.model.VentaOriginal;
import com.tienda.backend.devolucion.application.port.out.VentaDevolucionPort;
import com.tienda.backend.venta.application.model.ItemVentaComando;
import com.tienda.backend.venta.application.model.PagoVentaComando;
import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import com.tienda.backend.venta.application.model.ResultadoRegistroVenta;
import com.tienda.backend.venta.application.port.in.RegistrarVentaUseCase;
import com.tienda.backend.venta.application.port.out.VentaRepositoryPort;
import com.tienda.backend.venta.domain.model.EstadoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DevolucionVentaAdapter implements VentaDevolucionPort {
    private final VentaRepositoryPort ventaRepositoryPort;
    private final RegistrarVentaUseCase registrarVentaUseCase;

    @Override
    public Optional<VentaOriginal> bloquearVenta(Long ventaId) {
        return ventaRepositoryPort.findByIdParaActualizar(ventaId).map(this::aVentaOriginal);
    }

    @Override
    public Optional<VentaCambioRef> resumenVenta(Long ventaId) {
        return ventaRepositoryPort.findById(ventaId)
                .map(v -> new VentaCambioRef(v.getId(), v.getNumeroVenta(), v.getTotal()));
    }

    @Override
    public ResultadoVentaCambio registrarVentaDeCambio(Long clienteId, List<ItemNuevoComando> items,
                                                       List<PagoCambioComando> pagos, BigDecimal credito) {
        ResultadoRegistroVenta r = registrarVentaUseCase.registrar(new RegistrarVentaComando(clienteId,
                items.stream().map(i -> new ItemVentaComando(i.presentacionId(), i.cantidad(), i.precioUnitario(), i.motivoPrecio())).toList(),
                pagos.stream().map(p -> new PagoVentaComando(p.metodoPagoId(), p.monto(), p.referencia())).toList(),
                null, credito));
        Venta v = r.vista().venta();
        return new ResultadoVentaCambio(new VentaCambioRef(v.getId(), v.getNumeroVenta(), v.getTotal()), r.creditoAplicado());
    }

    private VentaOriginal aVentaOriginal(Venta v) {
        return new VentaOriginal(v.getId(), v.getNumeroVenta(), v.getEstado() == EstadoVenta.ANULADA, v.getClienteId(),
                v.getDepositoId(), v.getFechaVenta(),
                v.getLineas().stream().map(l -> new VentaOriginal.LineaOriginal(l.id(), l.productoId(), l.presentacionId(),
                        l.cantidad(), l.factorConversion(), l.subtotal(),
                        l.lotes().stream().map(x -> new VentaOriginal.LoteOriginal(x.loteId(), x.cantidadBase())).toList()))
                        .toList());
    }
}
