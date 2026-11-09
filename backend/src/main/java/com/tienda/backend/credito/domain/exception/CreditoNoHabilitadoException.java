package com.tienda.backend.credito.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class CreditoNoHabilitadoException extends InvalidBusinessRuleException {
    public CreditoNoHabilitadoException(String cliente) {
        super("El cliente " + cliente + " no tiene el crédito habilitado.");
    }
}
