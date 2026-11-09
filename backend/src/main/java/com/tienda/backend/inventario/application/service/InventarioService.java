package com.tienda.backend.inventario.application.service;

import com.tienda.backend.inventario.application.model.ResultadoSalidaFefo;
import com.tienda.backend.inventario.application.model.SalidaFefoComando;
import com.tienda.backend.inventario.application.port.in.RegistrarSalidaFefoUseCase;
import com.tienda.backend.inventario.application.model.AjusteComando;
import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.SalidaComando;
import com.tienda.backend.inventario.application.model.TrasladoComando;
import com.tienda.backend.inventario.application.port.in.RegistrarAjusteUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarEntradaUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarSalidaUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarTrasladoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de escritura del inventario. A propósito NO es transaccional: cada operación se ejecuta en
 * {@link MovimientoInventarioEjecutor} (una transacción con las filas de stock bloqueadas) y recién después de
 * confirmarse se consultan los nombres para la respuesta, así los bloqueos no se mantienen mientras se arma.
 */
@Service
@RequiredArgsConstructor
public class InventarioService implements RegistrarEntradaUseCase, RegistrarSalidaUseCase,
        RegistrarTrasladoUseCase, RegistrarAjusteUseCase, RegistrarSalidaFefoUseCase {

    private final MovimientoInventarioEjecutor ejecutor;
    private final MovimientoVistaFactory vistaFactory;

    @Override
    public MovimientoDetallado registrarEntrada(EntradaComando comando) {
        return vistaFactory.construir(ejecutor.ejecutarEntrada(comando));
    }

    @Override
    public MovimientoDetallado registrarSalida(SalidaComando comando) {
        return vistaFactory.construir(ejecutor.ejecutarSalida(comando));
    }

    /** No arma la vista detallada: quien llama (ventas) solo necesita de qué lotes salió cada línea. */
    @Override
    public ResultadoSalidaFefo registrarSalidaFefo(SalidaFefoComando comando) {
        MovimientoInventarioEjecutor.FefoEjecutado r = ejecutor.ejecutarSalidaFefo(comando);
        return new ResultadoSalidaFefo(r.movimiento().getId(), r.asignaciones());
    }

    @Override
    public MovimientoDetallado registrarTraslado(TrasladoComando comando) {
        return vistaFactory.construir(ejecutor.ejecutarTraslado(comando));
    }

    @Override
    public MovimientoDetallado registrarAjuste(AjusteComando comando) {
        return vistaFactory.construir(ejecutor.ejecutarAjuste(comando));
    }
}
