package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.ProveedorRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProveedorResponse;
import com.tienda.backend.application.dto.response.ProveedorStatsResponse;

public interface ProveedorService {
    ProveedorResponse crear(ProveedorRequest request);
    ProveedorResponse obtenerPorId(Long id);
    PageResponse<ProveedorResponse> listar(int page, int size, String nombreEmpresa);
    ProveedorResponse actualizar(Long id, ProveedorRequest request);
    ProveedorResponse cambiarEstado(Long id, boolean estado);
    void eliminar(Long id);
    ProveedorStatsResponse obtenerEstadisticas();
}
