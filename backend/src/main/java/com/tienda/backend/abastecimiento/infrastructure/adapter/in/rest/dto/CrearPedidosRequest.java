package com.tienda.backend.abastecimiento.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.abastecimiento.domain.model.Escenario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CrearPedidosRequest(
        @NotNull(message = "El depósito de destino es obligatorio")
        Long depositoDestinoId,

        /** null = ESTANDAR */
        Escenario escenario,

        @Size(max = 2000, message = "La observación admite hasta 2000 caracteres")
        String observacion,

        @NotEmpty(message = "Elija al menos una sugerencia")
        List<@Valid Item> items
) {
    public record Item(
            @NotNull(message = "Falta el id de la sugerencia")
            Long sugerenciaId,

            /** null = la cantidad del escenario elegido */
            @Positive(message = "La cantidad debe ser mayor que cero")
            BigDecimal cantidad
    ) {
    }
}
