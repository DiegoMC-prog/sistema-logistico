package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.ItemEntradaComando;
import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.NuevoLoteComando;
import com.tienda.backend.inventario.application.port.in.RegistrarEntradaUseCase;
import com.tienda.backend.inventario.domain.model.MovimientoDetalle;
import com.tienda.backend.pedido.application.port.out.StockPedidoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Ingresa el stock de una recepción reutilizando el caso de uso de entradas de inventario (misma transacción). */
@Component
@RequiredArgsConstructor
public class PedidoStockAdapter implements StockPedidoPort {
    private static final String REFERENCIA_PEDIDO = "PEDIDO_PROVEEDOR";

    private final RegistrarEntradaUseCase registrarEntradaUseCase;

    @Override
    public List<Long> ingresar(Long depositoId, String observacion, Long pedidoId, List<ItemIngreso> items) {
        MovimientoDetallado movimiento = registrarEntradaUseCase.registrarEntrada(new EntradaComando(
                depositoId, observacion,
                items.stream().map(i -> new ItemEntradaComando(i.productoId(), null,
                        new NuevoLoteComando(i.numeroLote(), i.fechaVencimiento(), i.costoUnitario(),
                                i.porcentajeDescuentoVencimiento()),
                        null, i.cantidad())).toList(),
                REFERENCIA_PEDIDO, pedidoId));

        // Las líneas del movimiento salen en el orden en que se enviaron; se comprueba por el producto
        List<MovimientoDetalle> detalles = movimiento.movimiento().getDetalles();
        if (detalles.size() != items.size()) {
            throw new IllegalStateException("La entrada de inventario devolvió " + detalles.size()
                    + " líneas para " + items.size() + " enviadas");
        }
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).productoId().equals(detalles.get(i).productoId())) {
                throw new IllegalStateException("El orden de las líneas de la entrada de inventario no coincide");
            }
        }
        return detalles.stream().map(MovimientoDetalle::loteId).toList();
    }
}
