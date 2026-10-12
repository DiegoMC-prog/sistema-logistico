package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.ActualizarUsuarioRequest;
import com.tienda.backend.application.dto.request.CambiarPasswordRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.UsuarioResponse;

public interface UsuarioService {
    UsuarioResponse obtenerPorId(Long id);
    UsuarioResponse obtenerPorUsername(String username);
    PageResponse<UsuarioResponse> listar(int page, int size);
    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);
    void cambiarPassword(Long id, CambiarPasswordRequest request);
    UsuarioResponse cambiarEstado(Long id, boolean estado);
    void eliminar(Long id);
}
