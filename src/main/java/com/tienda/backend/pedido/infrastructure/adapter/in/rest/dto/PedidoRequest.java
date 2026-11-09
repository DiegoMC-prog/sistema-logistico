package com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.pedido.application.model.PedidoComando;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Crear o editar un pedido en borrador. Las cantidades van en unidad base del producto. */
public record PedidoRequest(
        @NotNull(message = "El proveedor es obligatorio")
        Long proveedorId,

        @NotNull(message = "El deposito de destino es obligatorio")
        Long depositoDestinoId,

        LocalDate fechaEntregaEstimada,

        @Size(max = 2000, message = "La observacion no debe tener mas de 2000 caracteres")
        String observacion,

        @NotNull(message = "Las lineas son obligatorias (pueden ir vacias en un borrador)")
        @Valid
        List<ItemPedidoRequest> items
) {
    public record ItemPedidoRequest(
            @NotNull(message = "El producto es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor que cero")
            @Digits(integer = 10, fraction = 4, message = "La cantidad admite hasta 10 enteros y 4 decimales")
            BigDecimal cantidad,

            @NotNull(message = "El costo unitario es obligatorio")
            @DecimalMin(value = "0", message = "El costo unitario no puede ser negativo")
            @Digits(integer = 10, fraction = 4, message = "El costo admite hasta 10 enteros y 4 decimales")
            BigDecimal costoUnitario
    ) {
    }

    public PedidoComando toComando() {
        return new PedidoComando(proveedorId, depositoDestinoId, fechaEntregaEstimada, observacion,
                items.stream().map(i -> new PedidoComando.Item(i.productoId(), i.cantidad(), i.costoUnitario())).toList());
    }
}
