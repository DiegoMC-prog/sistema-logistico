package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.credito.application.port.in.GestionarCuentaPorCobrarUseCase;
import com.tienda.backend.venta.application.port.out.CreditoVentaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class VentaCreditoAdapter implements CreditoVentaPort {
    private final GestionarCuentaPorCobrarUseCase cuentas;

    @Override
    public void crearCuenta(Long clienteId, Long ventaId, BigDecimal monto) {
        cuentas.crearParaVenta(clienteId, ventaId, monto);
    }

    @Override
    public void anularCuenta(Long ventaId) {
        cuentas.anularDeVenta(ventaId);
    }
}
