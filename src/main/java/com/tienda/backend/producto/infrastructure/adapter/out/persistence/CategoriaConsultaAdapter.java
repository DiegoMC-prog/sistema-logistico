package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.application.port.out.CategoriaRespositoryPort;
import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.producto.application.port.out.CategoriaConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoriaConsultaAdapter implements CategoriaConsultaPort {
    private final CategoriaRespositoryPort categoriaRespositoryPort;

    @Override
    public Optional<String> obtenerNombrePorId(Long categoriaId) {
        return categoriaRespositoryPort.findById(categoriaId).map(Categoria::getNombre);
    }
}
