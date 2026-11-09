/**
 * Permisos del backend (`MODULO_ACCION`, ver GET /permisos).
 * Los módulos de CRUD simple (catálogo, depósitos, clientes, precios, métodos de pago y cajas) se consultan con
 * cualquier usuario autenticado: por eso no tienen `VER` y solo se controlan las acciones de escritura.
 */

export interface PermisosCrud {
  crear: string;
  editar: string;
  eliminar: string;
  activar: string;
}

const crud = (modulo: string): PermisosCrud => ({
  crear: `${modulo}_CREAR`,
  editar: `${modulo}_EDITAR`,
  eliminar: `${modulo}_ELIMINAR`,
  activar: `${modulo}_ACTIVAR`,
});

/** Qué puede hacer el usuario sobre un módulo de CRUD (para mostrar u ocultar botones). */
export interface AccionesCrud {
  crear: boolean;
  editar: boolean;
  eliminar: boolean;
  activar: boolean;
}

export const MODULOS = {
  /** Categorías, productos, unidades, presentaciones y lotes. */
  catalogo: crud('CATALOGO'),
  deposito: crud('DEPOSITO'),
  /** Clientes y tipos de cliente. */
  cliente: crud('CLIENTE'),
  precio: crud('PRECIO'),
  metodoPago: crud('METODO_PAGO'),
  caja: crud('CAJA'),
} as const;

/** Cualquier permiso del módulo: decide si se muestra su sección en el menú. */
export const todosDe = (m: PermisosCrud): readonly string[] => Object.values(m);

export const PERMISOS = {
  usuarioVer: 'USUARIO_VER',
  usuarioCrear: 'USUARIO_CREAR',
  usuarioEditar: 'USUARIO_EDITAR',
  usuarioEliminar: 'USUARIO_ELIMINAR',
  usuarioActivar: 'USUARIO_ACTIVAR',
  usuarioRestablecerPassword: 'USUARIO_RESTABLECER_PASSWORD',
  usuarioAsignarRoles: 'USUARIO_ASIGNAR_ROLES',

  rolVer: 'ROL_VER',
  rolCrear: 'ROL_CREAR',
  rolEditar: 'ROL_EDITAR',
  rolEliminar: 'ROL_ELIMINAR',
  rolAsignarPermisos: 'ROL_ASIGNAR_PERMISOS',

  inventarioVer: 'INVENTARIO_VER',
  inventarioMover: 'INVENTARIO_MOVER',

  cajaAbrir: 'CAJA_ABRIR',
  cajaCerrar: 'CAJA_CERRAR',
  cajaMovimiento: 'CAJA_MOVIMIENTO',
  /** Ver y operar los turnos de todos los cajeros. */
  cajaSupervisar: 'CAJA_SUPERVISAR',

  ventaVer: 'VENTA_VER',
  ventaCrear: 'VENTA_CREAR',
  ventaAnular: 'VENTA_ANULAR',
  ventaDevolver: 'VENTA_DEVOLVER',
  ventaPrecioModificar: 'VENTA_PRECIO_MODIFICAR',
  ventaPrecioBajoCosto: 'VENTA_PRECIO_BAJO_COSTO',

  proveedorVer: 'PROVEEDOR_VER',
  proveedorCrear: 'PROVEEDOR_CREAR',
  proveedorEditar: 'PROVEEDOR_EDITAR',
  proveedorEliminar: 'PROVEEDOR_ELIMINAR',
  proveedorActivar: 'PROVEEDOR_ACTIVAR',

  pedidoVer: 'PEDIDO_VER',
  pedidoCrear: 'PEDIDO_CREAR',
  pedidoEditar: 'PEDIDO_EDITAR',
  pedidoEliminar: 'PEDIDO_ELIMINAR',
  pedidoEnviar: 'PEDIDO_ENVIAR',
  pedidoCancelar: 'PEDIDO_CANCELAR',
  pedidoRecibir: 'PEDIDO_RECIBIR',

  reporteVentas: 'REPORTE_VENTAS',
  reporteCaja: 'REPORTE_CAJA',
  reporteInventario: 'REPORTE_INVENTARIO',
  reporteUtilidad: 'REPORTE_UTILIDAD',
  reporteCompras: 'REPORTE_COMPRAS',
} as const;

/** Cualquier permiso de caja que permite consultar turnos. */
export const PERMISOS_CAJA: readonly string[] = [
  PERMISOS.cajaAbrir,
  PERMISOS.cajaCerrar,
  PERMISOS.cajaMovimiento,
  PERMISOS.cajaSupervisar,
];
