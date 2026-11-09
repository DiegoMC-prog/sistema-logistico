package com.tienda.backend.venta.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.venta.application.model.ItemVentaComando;
import com.tienda.backend.venta.application.model.PagoVentaComando;
import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record VentaRequest(
        /** Opcional: sin cliente se vende al precio general. */
        Long clienteId,

        @NotEmpty(message = "La venta debe tener al menos una linea")
        @Size(max = 100, message = "Una venta admite hasta 100 lineas")
        List<@Valid ItemRequest> items,

        /** Puede omitirse solo si la venta no tiene importe a cobrar. */
        @Size(max = 10, message = "Una venta admite hasta 10 pagos")
        List<@Valid PagoRequest> pagos
) {
    public record ItemRequest(
            @NotNull(message = "La presentacion es obligatoria")
            Long presentacionId,

            @NotNull(message = "La cantidad es obligatoria")
            @DecimalMin(value = "0", inclusive = false, message = "La cantidad debe ser mayor a cero")
            @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
            BigDecimal cantidad,

            /** Precio manual: reemplaza al de la lista solo en esta venta (requiere permiso y motivo). */
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

    public RegistrarVentaComando toComando(String idempotencyKey) {
        return new RegistrarVentaComando(clienteId,
                items.stream().map(i -> new ItemVentaComando(i.presentacionId(), i.cantidad(), i.precioUnitario(), i.motivoPrecio())).toList(),
                pagos == null ? List.of()
                        : pagos.stream().map(p -> new PagoVentaComando(p.metodoPagoId(), p.monto(), p.referencia())).toList(),
                idempotencyKey);
    }
}
