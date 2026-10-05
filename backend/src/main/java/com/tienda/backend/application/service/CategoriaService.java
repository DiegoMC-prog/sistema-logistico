package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse obtenerPorId(Long id);

    Page<CategoriaResponse> obtenerTodas(Pageable pageable);

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    void eliminar(Long id);
}
