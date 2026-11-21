package com.tienda.backend.abastecimiento.domain.model;

/** Resultado de evaluar un producto. */
public enum EstadoSugerencia {
    /** La posición de inventario está en o bajo el punto de reorden (escenario estándar): conviene pedir. */
    REORDENAR,
    /** Hay stock suficiente para cubrir el lead time con el nivel de servicio pedido. */
    SIN_NECESIDAD,
    /** Falta historial de ventas o no hay costo de compra registrado. */
    DATOS_INSUFICIENTES,
    /** No se vendió nada en la ventana de análisis. */
    SIN_DEMANDA,
    /** El producto nunca se pidió a un proveedor y no tiene uno asignado. */
    SIN_PROVEEDOR
}
