package com.tienda.backend.categoria.application.service;

import com.tienda.backend.categoria.application.port.in.CrearCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.EditarCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.EliminarCategoriaUseCase;
import com.tienda.backend.categoria.application.port.in.ListarCategoriaUseCase;
import com.tienda.backend.categoria.application.port.out.CategoriaRespositoryPort;
import com.tienda.backend.categoria.domain.exception.CategoriaNombreDuplicadoException;
import com.tienda.backend.categoria.domain.exception.CategoriaNotFoundException;
import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.categoria.application.model.CategoriaFiltro;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CategoriaService implements CrearCategoriaUseCase, EditarCategoriaUseCase, ListarCategoriaUseCase, EliminarCategoriaUseCase {

    private final CategoriaRespositoryPort categoriaRespositoryPort;

    @Override
    @Transactional
    public Categoria create(Categoria categoria) {
        if (categoriaRespositoryPort.existsByNombre(categoria.getNombre())) {
            throw new CategoriaNombreDuplicadoException(categoria.getNombre());
        }
        return categoriaRespositoryPort.save(categoria);
    }

    @Override
    @Transactional
    public Categoria editar(Long id, Categoria categoria) {
        Categoria existente = categoriaRespositoryPort.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));

        if (categoriaRespositoryPort.existsByNombreAndIdNot(categoria.getNombre(), id)) {
            throw new CategoriaNombreDuplicadoException(categoria.getNombre());
        }

        existente.editarNombre(categoria.getNombre());

        return categoriaRespositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Categoria> listar(CategoriaFiltro filtro, int page, int size) {
        return categoriaRespositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        categoriaRespositoryPort.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
        categoriaRespositoryPort.deleteById(id);
    }
}
