package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;
import com.tienda.backend.application.dto.response.CategoriaStatsResponse;
import com.tienda.backend.application.dto.response.PageResponse;

public interface CategoriaService {
    CategoriaResponse crear(CategoriaRequest categoriaRequest);
    CategoriaResponse obtenerPorId(Long id);
    PageResponse<CategoriaResponse> listar(int page, int size, String nombre);
    CategoriaResponse actualizar(Long id, CategoriaRequest categoriaRequest);
    void eliminar(Long id);
    CategoriaStatsResponse obtenerEstadisticas();
}
