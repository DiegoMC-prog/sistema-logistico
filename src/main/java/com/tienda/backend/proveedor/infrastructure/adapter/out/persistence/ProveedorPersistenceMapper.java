package com.tienda.backend.proveedor.infrastructure.adapter.out.persistence;

import com.tienda.backend.proveedor.domain.model.EstadoProveedor;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorPersistenceMapper {
    public Proveedor toDomain(ProveedorEntity entity) {
        if (entity == null) return null;
        return new Proveedor(entity.getId(), entity.getNombreEmpresa(), entity.getNombreContacto(),
                entity.getTelefono(), entity.getEmail(),
                entity.isActivo() ? EstadoProveedor.ACTIVO : EstadoProveedor.INACTIVO);
    }

    public ProveedorEntity toEntity(Proveedor domain) {
        if (domain == null) return null;
        return ProveedorEntity.builder()
                .id(domain.getId())
                .nombreEmpresa(domain.getNombreEmpresa())
                .nombreContacto(domain.getNombreContacto())
                .telefono(domain.getTelefono())
                .email(domain.getEmail())
                .activo(domain.estaActivo())
                .build();
    }
}
