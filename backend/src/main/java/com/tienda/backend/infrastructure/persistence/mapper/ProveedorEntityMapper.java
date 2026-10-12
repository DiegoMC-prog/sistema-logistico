package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Proveedor;
import com.tienda.backend.infrastructure.persistence.entity.ProveedorEntity;
import org.springframework.stereotype.Component;

@Component
public class ProveedorEntityMapper {

    public ProveedorEntity toEntity(Proveedor domain) {
        if (domain == null) {
            return null;
        }

        return new ProveedorEntity(
                domain.getId(),
                domain.getNombreEmpresa(),
                domain.getDireccionEmpresa(),
                domain.getTelefonoEmpresa(),
                domain.getNombreContacto(),
                domain.getTelefonoContacto(),
                domain.getEstado()
        );
    }

    public Proveedor toDomain(ProveedorEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Proveedor(
                entity.getId(),
                entity.getNombreEmpresa(),
                entity.getDireccionEmpresa(),
                entity.getTelefonoEmpresa(),
                entity.getNombreContacto(),
                entity.getTelefonoContacto(),
                entity.getEstado()
        );
    }
}
