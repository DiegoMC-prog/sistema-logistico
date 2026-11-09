package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.inventario.application.port.out.LoteGestionPort;
import com.tienda.backend.lote.application.model.CrearLoteComando;
import com.tienda.backend.lote.application.port.in.CrearLoteUseCase;
import com.tienda.backend.lote.domain.model.Lote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Crea lotes a través del caso de uso del módulo lote, así se aplican todas sus reglas. */
@Component
@RequiredArgsConstructor
public class InventarioLoteGestionAdapter implements LoteGestionPort {
    private final CrearLoteUseCase crearLoteUseCase;

    @Override
    public Lote crearLote(CrearLoteComando comando) {
        return crearLoteUseCase.create(comando).lote();
    }
}
