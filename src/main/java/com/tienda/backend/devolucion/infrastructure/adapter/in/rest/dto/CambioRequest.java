package com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.devolucion.application.model.ItemNuevoComando;
import com.tienda.backend.devolucion.application.model.PagoCambioComando;
import com.tienda.backend.devolucion.application.model.RegistrarCambioComando;
import com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto.DevolucionRequest.ItemDevueltoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CambioRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 255, message = "El motivo no debe tener mas de 255 caracteres")
        String motivo,

        @NotEmpty(message = "Debe indicar al menos una linea a devolver")
        @Size(max = 100, message = "Se admiten hasta 100 lineas")
        List<@Valid ItemDevueltoRequest> devueltos,

        @NotEmpty(message = "Debe indicar al menos un producto nuevo")
        @Size(max = 100, message = "Se admiten hasta 100 lineas")
        List<@Valid ItemNuevoRequest> nuevos,

        /** Pago de la diferencia, solo si lo nuevo vale mas que lo devuelto. */
        @Size(max = 10, message = "Se admiten hasta 10 pagos")
        List<@Valid PagoRequest> pagos
) {
    public record ItemNuevoRequest(
            @NotNull(message = "La presentacion es obligatoria")
            Long presentacionId,

            @NotNull(message = "La cantidad es obligatoria")
            @DecimalMin(value = "0", inclusive = false, message = "La cantidad debe ser mayor a cero")
            @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
            BigDecimal cantidad,

            @DecimalMin(value = "0", message = "El precio no puede ser negativo")
            @Digits(integer = 10, fraction = 4, message = "El precio admite hasta 10 enteros y 4 decimales")
            BigDecimal precioUnitario,

            @Size(max = 150, message = "El motivo del precio no debe tener mas de 150 caracteres")
            String motivoPrecio
    ) {
    }

    public record PagoRequest(
            @NotNull(message = "El metodo de pago es obligatorio")
            Long metodoPagoId,

            @NotNull(message = "El monto es obligatorio")
            @DecimalMin(value = "0", inclusive = false, message = "El monto debe ser mayor a cero")
            @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 10 enteros y 2 decimales")
            BigDecimal monto,

            @Size(max = 100, message = "La referencia no debe tener mas de 100 caracteres")
            String referencia
    ) {
    }

    public RegistrarCambioComando toComando(Long ventaId, String idempotencyKey) {
        return new RegistrarCambioComando(ventaId, motivo,
                devueltos.stream().map(ItemDevueltoRequest::toComando).toList(),
                nuevos.stream().map(n -> new ItemNuevoComando(n.presentacionId(), n.cantidad(), n.precioUnitario(), n.motivoPrecio())).toList(),
                pagos == null ? List.of()
                        : pagos.stream().map(p -> new PagoCambioComando(p.metodoPagoId(), p.monto(), p.referencia())).toList(),
                idempotencyKey);
    }
}
