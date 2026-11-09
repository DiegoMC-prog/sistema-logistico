package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.ItemStock;
import com.tienda.backend.venta.application.model.ReposicionStock;
import com.tienda.backend.venta.application.port.out.StockVentaPort;
import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.ItemEntradaComando;
import com.tienda.backend.inventario.application.model.ItemFefoComando;
import com.tienda.backend.inventario.application.model.ResultadoSalidaFefo;
import com.tienda.backend.inventario.application.model.SalidaFefoComando;
import com.tienda.backend.inventario.application.port.in.RegistrarEntradaUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarSalidaFefoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VentaStockAdapter implements StockVentaPort {
    private static final String REFERENCIA_VENTA = "VENTA";
    private static final String REFERENCIA_ANULACION = "ANULACION_VENTA";

    private final RegistrarSalidaFefoUseCase registrarSalidaFefoUseCase;
    private final RegistrarEntradaUseCase registrarEntradaUseCase;

    @Override
    public List<AsignacionStock> salir(Long depositoId, String motivo, List<ItemStock> items, Long ventaId) {
        ResultadoSalidaFefo resultado = registrarSalidaFefoUseCase.registrarSalidaFefo(new SalidaFefoComando(
                depositoId, motivo,
                items.stream().map(i -> new ItemFefoComando(i.productoId(), i.presentacionId(), i.cantidad())).toList(),
                REFERENCIA_VENTA, ventaId));
        return resultado.asignaciones().stream()
                .map(a -> new AsignacionStock(a.itemIndice(), a.loteId(), a.cantidadBase(), a.costoUnitario(),
                        a.porcentajeDescuentoVencimiento()))
                .toList();
    }

    @Override
    public void reponer(Long depositoId, String observacion, List<ReposicionStock> items, Long ventaId) {
        registrarEntradaUseCase.registrarEntrada(new EntradaComando(depositoId, observacion,
                items.stream().map(i -> new ItemEntradaComando(i.productoId(), i.loteId(), null, null, i.cantidadBase())).toList(),
                REFERENCIA_ANULACION, ventaId));
    }
}
