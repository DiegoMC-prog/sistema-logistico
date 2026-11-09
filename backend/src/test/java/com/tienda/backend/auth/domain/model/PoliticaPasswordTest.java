package com.tienda.backend.auth.domain.model;

import com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliticaPasswordTest {

    @Test
    void aceptaDeOchoCaracteresAlLimiteDeBytesDeBcrypt() {
        assertDoesNotThrow(() -> PoliticaPassword.validar("12345678"));
        assertDoesNotThrow(() -> PoliticaPassword.validar("a".repeat(72)));
    }

    @Test
    void rechazaVaciaCortaOQueSuperaElLimite() {
        assertThrows(CambioPasswordInvalidoException.class, () -> PoliticaPassword.validar(null));
        assertThrows(CambioPasswordInvalidoException.class, () -> PoliticaPassword.validar("   "));
        assertThrows(CambioPasswordInvalidoException.class, () -> PoliticaPassword.validar("1234567"));
        assertThrows(CambioPasswordInvalidoException.class, () -> PoliticaPassword.validar("a".repeat(73)));
    }

    @Test
    void elLimiteSeMideEnBytesNoEnCaracteres() {
        // 40 caracteres de 2 bytes = 80 bytes: pasa de los 72 aunque tenga menos de 72 caracteres
        assertThrows(CambioPasswordInvalidoException.class, () -> PoliticaPassword.validar("ñ".repeat(40)));
    }
}
