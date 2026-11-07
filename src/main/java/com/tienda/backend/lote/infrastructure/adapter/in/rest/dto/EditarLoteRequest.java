package com.tienda.backend.lote.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.lote.application.model.EditarLoteComando;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EditarLoteRequest(
        @NotBlank(message = "El numero de lote es obligatorio")
        @Size(max = 80, message = "El numero de lote no debe tener mas de 80 caracteres")
        String numeroLote,

        LocalDate fechaVencimiento,

        @NotNull(message = "El costo unitario de compra es obligatorio")
        @PositiveOrZero(message = "El costo unitario de compra no puede ser negativo")
        @Digits(integer = 10, fraction = 4, message = "El costo admite hasta 10 enteros y 4 decimales")
        BigDecimal costoUnitarioCompra,

        @PositiveOrZero(message = "El descuento por vencimiento no puede ser negativo")
        @DecimalMax(value = "100", message = "El descuento por vencimiento no puede superar 100")
        @Digits(integer = 3, fraction = 4, message = "El descuento admite hasta 3 enteros y 4 decimales")
        BigDecimal porcentajeDescuentoVencimiento
) {
    public EditarLoteComando toComando() {
        return new EditarLoteComando(numeroLote, fechaVencimiento, costoUnitarioCompra, porcentajeDescuentoVencimiento);
    }
}
