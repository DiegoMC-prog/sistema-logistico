package com.tienda.backend.lote.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class LoteConStockException extends BusinessConflictException {
    public LoteConStockException() {
        super("No se puede eliminar un lote que todavía tiene stock. Retire o ajuste el stock primero.");
    }
}
