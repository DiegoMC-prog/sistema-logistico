package com.tienda.backend.shared.infrastructure.config.security;

/**
 * Nombres de los permisos del sistema. Deben coincidir con los de la tabla {@code permisos} (semilla de la migración),
 * porque viajan en el token y {@code SecurityConfig} los exige por endpoint.
 */
public final class Permisos {
    public static final String USUARIO_GESTIONAR = "USUARIO_GESTIONAR";
    public static final String ROL_GESTIONAR = "ROL_GESTIONAR";
    public static final String CATALOGO_GESTIONAR = "CATALOGO_GESTIONAR";
    public static final String DEPOSITO_GESTIONAR = "DEPOSITO_GESTIONAR";
    public static final String INVENTARIO_VER = "INVENTARIO_VER";
    public static final String INVENTARIO_MOVER = "INVENTARIO_MOVER";
    public static final String CLIENTE_GESTIONAR = "CLIENTE_GESTIONAR";
    public static final String METODO_PAGO_GESTIONAR = "METODO_PAGO_GESTIONAR";
    public static final String PRECIO_GESTIONAR = "PRECIO_GESTIONAR";
    public static final String CAJA_ABRIR = "CAJA_ABRIR";
    public static final String CAJA_CERRAR = "CAJA_CERRAR";
    public static final String CAJA_MOVIMIENTO = "CAJA_MOVIMIENTO";
    public static final String CAJA_GESTIONAR = "CAJA_GESTIONAR";
    public static final String VENTA_CREAR = "VENTA_CREAR";
    public static final String VENTA_ANULAR = "VENTA_ANULAR";
    public static final String VENTA_VER = "VENTA_VER";
    public static final String VENTA_DEVOLVER = "VENTA_DEVOLVER";

    private Permisos() {
    }
}
