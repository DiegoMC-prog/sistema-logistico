package com.tienda.backend.lote.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

public class LoteProductoSinPresentacionBaseException extends InvalidBusinessRuleException {
    public LoteProductoSinPresentacionBaseException(Long productoId) {
        super("El producto con ID " + productoId + " no tiene presentación base; defínala antes de crear lotes.");
    }
}
