package com.tienda.backend.devolucion.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class VentaFueraDePlazoException extends BusinessConflictException {
    public VentaFueraDePlazoException(int dias) {
        super("La venta superó el plazo de " + dias + " días para devoluciones.");
    }
}
