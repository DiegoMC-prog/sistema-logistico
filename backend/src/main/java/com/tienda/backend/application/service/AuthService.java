package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.LoginRequest;
import com.tienda.backend.application.dto.request.RegisterRequest;
import com.tienda.backend.application.dto.response.AuthResponse;
import com.tienda.backend.application.dto.response.UsuarioResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    UsuarioResponse register(RegisterRequest request);
}
