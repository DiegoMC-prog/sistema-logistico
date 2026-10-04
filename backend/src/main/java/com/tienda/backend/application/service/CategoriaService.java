package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;

import java.util.List;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse obtenerPorId(Long id);

    List<CategoriaResponse> obtenerTodas();

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    void eliminar(Long id);
}
