package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.ReposicionStock;
import com.tienda.backend.devolucion.application.port.out.StockDevolucionPort;
import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.ItemEntradaComando;
import com.tienda.backend.inventario.application.port.in.RegistrarEntradaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DevolucionStockAdapter implements StockDevolucionPort {
    private static final String REFERENCIA = "DEVOLUCION_VENTA";

    private final RegistrarEntradaUseCase registrarEntradaUseCase;

    @Override
    public void reponer(Long depositoId, String observacion, List<ReposicionStock> items, Long devolucionId) {
        registrarEntradaUseCase.registrarEntrada(new EntradaComando(depositoId, observacion,
                items.stream().map(i -> new ItemEntradaComando(i.productoId(), i.loteId(), null, null, i.cantidadBase())).toList(),
                REFERENCIA, devolucionId));
    }
}
