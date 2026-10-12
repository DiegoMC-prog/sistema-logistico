package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.application.dto.response.DepositoStatsResponse;
import com.tienda.backend.application.dto.response.PageResponse;

public interface DepositoService {
    DepositoResponse crear(DepositoRequest request);
    DepositoResponse obtenerPorId(Long id);
    PageResponse<DepositoResponse> listar(int page, int size, String nombre);
    DepositoResponse actualizar(Long id, DepositoRequest request);
    DepositoResponse cambiarEstado(Long id, boolean estado);
    void eliminar(Long id);
    DepositoStatsResponse obtenerEstadisticas();
}
