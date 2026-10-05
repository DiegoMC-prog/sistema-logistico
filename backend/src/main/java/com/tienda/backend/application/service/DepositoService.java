package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepositoService {

    DepositoResponse crear(DepositoRequest request);

    DepositoResponse obtenerPorId(Long id);

    Page<DepositoResponse> listarTodo(Pageable pageable);

    DepositoResponse actualizar(Long id, DepositoRequest request);

    void eliminar(Long id);
}
