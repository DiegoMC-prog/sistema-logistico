package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.cliente.domain.model.Cliente;
import com.tienda.backend.cliente.domain.model.EstadoCliente;
import com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence.TipoClienteEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientePersistenceMapper {
    public Cliente toDomain(ClienteEntity e) {
        if (e == null) return null;
        return new Cliente(e.getId(), e.getTipoCliente().getId(), e.getDocumento(), e.getNombre(),
                e.getTelefono(), e.getEmail(), EstadoCliente.valueOf(e.getEstado()), e.isCreditoHabilitado(), e.getLimiteCredito());
    }

    public ClienteEntity toEntity(Cliente d) {
        if (d == null) return null;
        return ClienteEntity.builder()
                .id(d.getId())
                .tipoCliente(TipoClienteEntity.builder().id(d.getTipoClienteId()).build())
                .documento(d.getDocumento())
                .nombre(d.getNombre())
                .telefono(d.getTelefono())
                .email(d.getEmail())
                .estado(d.getEstado().name())
                .creditoHabilitado(d.isCreditoHabilitado())
                .limiteCredito(d.getLimiteCredito())
                .build();
    }
}
