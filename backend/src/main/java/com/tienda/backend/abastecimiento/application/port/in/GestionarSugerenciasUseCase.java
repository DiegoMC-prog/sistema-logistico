package com.tienda.backend.abastecimiento.application.port.in;

import com.tienda.backend.abastecimiento.application.model.CrearPedidosComando;
import com.tienda.backend.abastecimiento.application.model.PedidoGenerado;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;

import java.util.List;

public interface GestionarSugerenciasUseCase {
    /** Crea un pedido BORRADOR por proveedor con las sugerencias elegidas. */
    List<PedidoGenerado> crearPedidos(CrearPedidosComando comando);

    SugerenciaResumen descartar(Long id);
}
