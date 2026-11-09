/** Nombres de permisos del backend (GET /permisos). */
export const PERMISOS = {
  catalogo: 'CATALOGO_GESTIONAR',
  deposito: 'DEPOSITO_GESTIONAR',
  usuario: 'USUARIO_GESTIONAR',
  rol: 'ROL_GESTIONAR',
  inventarioVer: 'INVENTARIO_VER',
  inventarioMover: 'INVENTARIO_MOVER',
  cliente: 'CLIENTE_GESTIONAR',
  metodoPago: 'METODO_PAGO_GESTIONAR',
  precio: 'PRECIO_GESTIONAR',
  cajaGestionar: 'CAJA_GESTIONAR',
  cajaAbrir: 'CAJA_ABRIR',
  cajaCerrar: 'CAJA_CERRAR',
  cajaMovimiento: 'CAJA_MOVIMIENTO',
  ventaCrear: 'VENTA_CREAR',
  ventaVer: 'VENTA_VER',
  ventaAnular: 'VENTA_ANULAR',
  ventaDevolver: 'VENTA_DEVOLVER',
  ventaPrecioModificar: 'VENTA_PRECIO_MODIFICAR',
  ventaPrecioBajoCosto: 'VENTA_PRECIO_BAJO_COSTO',
} as const;

/** Cualquier permiso de caja: habilita consultar turnos. */
export const PERMISOS_CAJA: readonly string[] = [
  PERMISOS.cajaGestionar,
  PERMISOS.cajaAbrir,
  PERMISOS.cajaCerrar,
  PERMISOS.cajaMovimiento,
];
