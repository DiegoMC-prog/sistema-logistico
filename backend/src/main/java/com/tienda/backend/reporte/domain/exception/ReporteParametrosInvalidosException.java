package com.tienda.backend.reporte.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class ReporteParametrosInvalidosException extends InvalidBusinessRuleException {
    public ReporteParametrosInvalidosException(String message) {
        super(message);
    }
}
