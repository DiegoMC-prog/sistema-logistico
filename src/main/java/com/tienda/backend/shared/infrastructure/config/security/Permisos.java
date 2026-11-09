package com.tienda.backend.shared.infrastructure.config.security;

/**
 * Nombres de los permisos del sistema (MODULO_ACCION). Deben coincidir con los de la tabla {@code permisos}
 * (semilla de la migración), porque viajan en el token y {@code SecurityConfig} los exige por endpoint.
 */
public final class Permisos {
    // Usuarios
    public static final String USUARIO_VER = "USUARIO_VER";
    public static final String USUARIO_CREAR = "USUARIO_CREAR";
    public static final String USUARIO_EDITAR = "USUARIO_EDITAR";
    public static final String USUARIO_ELIMINAR = "USUARIO_ELIMINAR";
    public static final String USUARIO_ACTIVAR = "USUARIO_ACTIVAR";
    public static final String USUARIO_RESTABLECER_PASSWORD = "USUARIO_RESTABLECER_PASSWORD";
    public static final String USUARIO_ASIGNAR_ROLES = "USUARIO_ASIGNAR_ROLES";

    // Roles
    public static final String ROL_VER = "ROL_VER";
    public static final String ROL_CREAR = "ROL_CREAR";
    public static final String ROL_EDITAR = "ROL_EDITAR";
    public static final String ROL_ELIMINAR = "ROL_ELIMINAR";
    public static final String ROL_ASIGNAR_PERMISOS = "ROL_ASIGNAR_PERMISOS";

    // Catálogo (categorías, productos, unidades de medida, presentaciones y lotes)
    public static final String CATALOGO_CREAR = "CATALOGO_CREAR";
    public static final String CATALOGO_EDITAR = "CATALOGO_EDITAR";
    public static final String CATALOGO_ELIMINAR = "CATALOGO_ELIMINAR";
    public static final String CATALOGO_ACTIVAR = "CATALOGO_ACTIVAR";

    // Depósitos
    public static final String DEPOSITO_CREAR = "DEPOSITO_CREAR";
    public static final String DEPOSITO_EDITAR = "DEPOSITO_EDITAR";
    public static final String DEPOSITO_ELIMINAR = "DEPOSITO_ELIMINAR";
    public static final String DEPOSITO_ACTIVAR = "DEPOSITO_ACTIVAR";

    // Clientes y tipos de cliente
    public static final String CLIENTE_CREAR = "CLIENTE_CREAR";
    public static final String CLIENTE_EDITAR = "CLIENTE_EDITAR";
    public static final String CLIENTE_ELIMINAR = "CLIENTE_ELIMINAR";
    public static final String CLIENTE_ACTIVAR = "CLIENTE_ACTIVAR";

    // Precios
    public static final String PRECIO_CREAR = "PRECIO_CREAR";
    public static final String PRECIO_EDITAR = "PRECIO_EDITAR";
    public static final String PRECIO_ELIMINAR = "PRECIO_ELIMINAR";
    public static final String PRECIO_ACTIVAR = "PRECIO_ACTIVAR";

    // Métodos de pago
    public static final String METODO_PAGO_CREAR = "METODO_PAGO_CREAR";
    public static final String METODO_PAGO_EDITAR = "METODO_PAGO_EDITAR";
    public static final String METODO_PAGO_ELIMINAR = "METODO_PAGO_ELIMINAR";
    public static final String METODO_PAGO_ACTIVAR = "METODO_PAGO_ACTIVAR";

    // Inventario
    public static final String INVENTARIO_VER = "INVENTARIO_VER";
    public static final String INVENTARIO_MOVER = "INVENTARIO_MOVER";

    // Caja: administrar cajas y operar turnos
    public static final String CAJA_CREAR = "CAJA_CREAR";
    public static final String CAJA_EDITAR = "CAJA_EDITAR";
    public static final String CAJA_ELIMINAR = "CAJA_ELIMINAR";
    public static final String CAJA_ACTIVAR = "CAJA_ACTIVAR";
    public static final String CAJA_ABRIR = "CAJA_ABRIR";
    public static final String CAJA_CERRAR = "CAJA_CERRAR";
    public static final String CAJA_MOVIMIENTO = "CAJA_MOVIMIENTO";
    /** Ver y operar los turnos de otros usuarios. */
    public static final String CAJA_SUPERVISAR = "CAJA_SUPERVISAR";

    // Proveedores
    public static final String PROVEEDOR_VER = "PROVEEDOR_VER";
    public static final String PROVEEDOR_CREAR = "PROVEEDOR_CREAR";
    public static final String PROVEEDOR_EDITAR = "PROVEEDOR_EDITAR";
    public static final String PROVEEDOR_ELIMINAR = "PROVEEDOR_ELIMINAR";
    public static final String PROVEEDOR_ACTIVAR = "PROVEEDOR_ACTIVAR";

    // Ventas, devoluciones y cambios
    public static final String VENTA_VER = "VENTA_VER";
    public static final String VENTA_CREAR = "VENTA_CREAR";
    public static final String VENTA_ANULAR = "VENTA_ANULAR";
    public static final String VENTA_DEVOLVER = "VENTA_DEVOLVER";
    public static final String VENTA_PRECIO_MODIFICAR = "VENTA_PRECIO_MODIFICAR";
    public static final String VENTA_PRECIO_BAJO_COSTO = "VENTA_PRECIO_BAJO_COSTO";

    private Permisos() {
    }
}
