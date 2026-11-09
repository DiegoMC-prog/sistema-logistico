package com.tienda.backend.inventario.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class LoteNoPerteneceAProductoException extends InvalidBusinessRuleException {
    public LoteNoPerteneceAProductoException(Long loteId, Long productoId) {
        super("El lote " + loteId + " no pertenece al producto " + productoId + ".");
    }
}
