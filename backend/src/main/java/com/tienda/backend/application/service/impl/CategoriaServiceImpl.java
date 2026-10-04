package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;
import com.tienda.backend.application.mapper.CategoriaDtoMapper;
import com.tienda.backend.application.service.CategoriaService;
import com.tienda.backend.domain.exception.RecursoDuplicadoException;
import com.tienda.backend.domain.exception.RecursoNoEncontradoException;
import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaDtoMapper categoriaDtoMapper;


    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaDtoMapper categoriaDtoMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaDtoMapper = categoriaDtoMapper;
    }

    @Override
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existePorNombre(request.nombre())) {
            throw new RecursoDuplicadoException("La categoria ya existe");
        }

        Categoria categoria = categoriaDtoMapper.toDomain(request);
        Categoria categoriaGuardada = categoriaRepository.guardar(categoria);

        return categoriaDtoMapper.toResponse(categoriaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("No se encontro la categoria con el id " + id)
                );

        return categoriaDtoMapper.toResponse(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerTodas() {
        return categoriaRepository.buscarTodas()
                .stream()
                .map(categoriaDtoMapper::toResponse)
                .toList();
    }

    @Override
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el recurso con el id: " + id));

        if (!categoria.getNombre().equalsIgnoreCase(request.nombre())
                && categoriaRepository.existePorNombre(request.nombre())) {
            throw new RecursoDuplicadoException("el nombre esta duplicado");
        }

        categoria.actualizarNombre(request.nombre());
        Categoria categoriaActualizada = categoriaRepository.guardar(categoria);

        return categoriaDtoMapper.toResponse(categoriaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!categoriaRepository.existePorId(id)) {
            throw new RecursoNoEncontradoException("No existe el recurso con el id: " + id);
        }
        categoriaRepository.eliminar(id);
    }
}
