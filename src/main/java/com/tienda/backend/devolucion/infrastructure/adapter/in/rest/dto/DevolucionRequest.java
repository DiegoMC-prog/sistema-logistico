package com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.devolucion.application.model.ItemDevueltoComando;
import com.tienda.backend.devolucion.application.model.RegistrarDevolucionComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record DevolucionRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no debe tener mas de 255 caracteres")
        String motivo,

        /** Por defecto true. Con false no se reembolsa dinero (por ejemplo, un canje fuera del sistema). */
        Boolean reembolsar,

        /** Obligatorio si hay reembolso. No puede ser el credito de devolucion. */
        Long metodoPagoId,

        @NotEmpty(message = "Debe indicar al menos una linea a devolver")
        @Size(max = 100, message = "Se admiten hasta 100 lineas")
        List<@Valid ItemDevueltoRequest> items
) {
    public RegistrarDevolucionComando toComando(Long ventaId, String idempotencyKey) {
        return new RegistrarDevolucionComando(ventaId, motivo, reembolsar == null || reembolsar, metodoPagoId,
                items.stream().map(ItemDevueltoRequest::toComando).toList(), idempotencyKey);
    }

    public record ItemDevueltoRequest(
            @NotNull(message = "La linea de la venta es obligatoria")
            Long ventaDetalleId,

            @NotNull(message = "La cantidad es obligatoria")
            @DecimalMin(value = "0", inclusive = false, message = "La cantidad debe ser mayor a cero")
            @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
            BigDecimal cantidad,

            /** Por defecto true. Con false (producto danado o vencido) no vuelve al stock. */
            Boolean reingresaStock
    ) {
        public ItemDevueltoComando toComando() {
            return new ItemDevueltoComando(ventaDetalleId, cantidad, reingresaStock == null || reingresaStock);
        }
    }
}
