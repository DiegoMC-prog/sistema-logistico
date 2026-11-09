package com.tienda.backend.cliente.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.cliente.domain.model.Cliente;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        Long tipoClienteId,

        @Size(max = 30, message = "El documento no debe tener mas de 30 caracteres")
        String documento,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no debe tener mas de 150 caracteres")
        String nombre,

        @Size(max = 30, message = "El telefono no debe tener mas de 30 caracteres")
        String telefono,

        @Size(max = 255, message = "El email no debe tener mas de 255 caracteres")
        String email,

        /** Opcional: sin valor, el cliente no tiene crédito. */
        Boolean creditoHabilitado,

        @DecimalMin(value = "0", message = "El limite de credito no puede ser negativo")
        BigDecimal limiteCredito
) {
    public Cliente toDomain() {
        return new Cliente(tipoClienteId, documento, nombre, telefono, email, Boolean.TRUE.equals(creditoHabilitado),
                limiteCredito);
    }
}
