package com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.pedido.application.model.RecepcionComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Recibir mercadería de un pedido. Cada línea crea un lote nuevo con el costo pactado en el pedido. */
public record RecepcionRequest(
        @Size(max = 80, message = "El numero de guia no debe tener mas de 80 caracteres")
        String numeroGuia,

        @Size(max = 2000, message = "La observacion no debe tener mas de 2000 caracteres")
        String observacion,

        @NotEmpty(message = "Debe indicar al menos una linea")
        @Valid
        List<ItemRecepcionRequest> items
) {
    public record ItemRecepcionRequest(
            @NotNull(message = "La linea del pedido es obligatoria")
            Long lineaId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor que cero")
            @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
            BigDecimal cantidad,

            @Size(max = 100, message = "El numero de lote no debe tener mas de 100 caracteres")
            String numeroLote,

            LocalDate fechaVencimiento,

            @DecimalMin(value = "0", message = "El descuento por vencimiento no puede ser negativo")
            @DecimalMax(value = "100", message = "El descuento por vencimiento no puede superar 100")
            @Digits(integer = 3, fraction = 2, message = "El descuento admite hasta 3 enteros y 2 decimales")
            BigDecimal porcentajeDescuentoVencimiento
    ) {
    }

    public RecepcionComando toComando(Long pedidoId) {
        return new RecepcionComando(pedidoId, numeroGuia, observacion,
                items.stream().map(i -> new RecepcionComando.Item(i.lineaId(), i.cantidad(), i.numeroLote(),
                        i.fechaVencimiento(), i.porcentajeDescuentoVencimiento())).toList());
    }
}
