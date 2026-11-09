package com.tienda.backend.metodopago.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class MetodoPagoEnUsoException extends BusinessConflictException {
    public MetodoPagoEnUsoException() {
        super("El método de pago no se puede eliminar porque ya se usó en ventas o devoluciones. Puede desactivarlo.");
    }
}
