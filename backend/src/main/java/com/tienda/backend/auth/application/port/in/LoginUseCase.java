package com.tienda.backend.auth.application.port.in;

import com.tienda.backend.auth.application.model.LoginComando;
import com.tienda.backend.auth.application.model.ParTokens;

public interface LoginUseCase {
    ParTokens login(LoginComando comando);
}
