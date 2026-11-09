package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.SalidaComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SalidaRequest(
        @NotNull(message = "El deposito es obligatorio")
        Long depositoId,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        @NotEmpty(message = "Debe indicar al menos una linea")
        @Valid
        List<ItemMovimientoRequest> items
) {
    public SalidaComando toComando() {
        return new SalidaComando(depositoId, motivo,
                items.stream().map(ItemMovimientoRequest::toComando).toList(), null, null);
    }
}
