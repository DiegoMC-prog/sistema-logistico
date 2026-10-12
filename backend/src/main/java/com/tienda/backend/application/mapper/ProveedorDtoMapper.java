package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.ProveedorRequest;
import com.tienda.backend.application.dto.response.ProveedorResponse;
import com.tienda.backend.domain.model.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorDtoMapper {

    public Proveedor toDomain(ProveedorRequest request) {
        return Proveedor.crear(
                request.nombreEmpresa(),
                request.direccionEmpresa(),
                request.telefonoEmpresa(),
                request.nombreContacto(),
                request.telefonoContacto()
        );
    }

    public ProveedorResponse toResponse(Proveedor domain) {
        return new ProveedorResponse(
                domain.getId(),
                domain.getNombreEmpresa(),
                domain.getDireccionEmpresa(),
                domain.getTelefonoEmpresa(),
                domain.getNombreContacto(),
                domain.getTelefonoContacto(),
                domain.getEstado()
        );
    }
}
