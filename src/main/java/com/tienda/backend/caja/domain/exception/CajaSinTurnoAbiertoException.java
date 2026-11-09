package com.tienda.backend.caja.domain.exception;

import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;

public class CajaSinTurnoAbiertoException extends ResourceNotFoundException {
    public CajaSinTurnoAbiertoException(Long cajaId) {
        super("La caja " + cajaId + " no tiene un turno abierto.");
    }
}
