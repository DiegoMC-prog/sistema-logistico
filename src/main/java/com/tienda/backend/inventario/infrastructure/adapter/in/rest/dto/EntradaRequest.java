package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.EntradaComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EntradaRequest(
        @NotNull(message = "El deposito es obligatorio")
        Long depositoId,

        String observacion,

        @NotEmpty(message = "Debe indicar al menos una linea")
        @Valid
        List<ItemEntradaRequest> items
) {
    /** La referencia (venta, pedido...) no se acepta por la API pública: solo la fijan casos de uso internos. */
    public EntradaComando toComando() {
        return new EntradaComando(depositoId, observacion,
                items.stream().map(ItemEntradaRequest::toComando).toList(), null, null);
    }
}
