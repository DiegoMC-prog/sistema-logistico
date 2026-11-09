package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.TrasladoComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrasladoRequest(
        @NotNull(message = "El deposito de origen es obligatorio")
        Long depositoOrigenId,

        @NotNull(message = "El deposito de destino es obligatorio")
        Long depositoDestinoId,

        String observacion,

        @NotEmpty(message = "Debe indicar al menos una linea")
        @Valid
        List<ItemMovimientoRequest> items
) {
    public TrasladoComando toComando() {
        return new TrasladoComando(depositoOrigenId, depositoDestinoId, observacion,
                items.stream().map(ItemMovimientoRequest::toComando).toList());
    }
}
