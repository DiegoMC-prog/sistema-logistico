package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class PresentacionNoPerteneceAProductoException extends InvalidBusinessRuleException {
    public PresentacionNoPerteneceAProductoException(Long presentacionId, Long productoId) {
        super("La presentación " + presentacionId + " no pertenece al producto " + productoId + ".");
    }
}
