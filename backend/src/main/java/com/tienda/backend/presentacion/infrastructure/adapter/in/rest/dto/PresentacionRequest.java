package com.tienda.backend.presentacion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.presentacion.domain.model.Presentacion;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PresentacionRequest(
        @NotNull(message = "La unidad de medida es obligatoria")
        Long unidadMedidaId,

        @NotBlank(message = "El codigo de barras es obligatorio")
        @Size(max = 64, message = "El codigo de barras no debe tener mas de 64 caracteres")
        String codigoBarras,

        @NotNull(message = "El factor de conversion es obligatorio")
        @Positive(message = "El factor de conversion debe ser mayor que cero")
        @Digits(integer = 8, fraction = 4, message = "El factor de conversion admite hasta 8 enteros y 4 decimales")
        BigDecimal factorConversion,

        @NotNull(message = "Debe indicar si es la unidad base")
        Boolean esUnidadBase
) {
    public Presentacion toDomain(Long productoId) {
        return new Presentacion(productoId, unidadMedidaId, codigoBarras, factorConversion, esUnidadBase);
    }
}
