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

    private Permisos() {
    }
}
