package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.credito.application.port.in.GestionarCuentaPorCobrarUseCase;
import com.tienda.backend.devolucion.application.port.out.CreditoDevolucionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DevolucionCreditoAdapter implements CreditoDevolucionPort {
    private final GestionarCuentaPorCobrarUseCase cuentas;

    @Override
    public BigDecimal descontarDeuda(Long ventaId, BigDecimal monto) {
        return cuentas.descontarPorDevolucion(ventaId, monto);
    }
}
