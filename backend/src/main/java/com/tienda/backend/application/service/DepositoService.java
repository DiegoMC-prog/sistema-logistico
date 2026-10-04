package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;

import java.util.List;

public interface DepositoService {

    DepositoResponse crear(DepositoRequest request);

    DepositoResponse obtenerPorId(Long id);

    List<DepositoResponse> listarTodo();

    DepositoResponse actualizar(Long id, DepositoRequest request);

    void eliminar(Long id);
}
