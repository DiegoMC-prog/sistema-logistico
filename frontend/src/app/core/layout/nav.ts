import { IconName } from '../../shared/ui/icon';
import { MODULOS, PERMISOS, PERMISOS_CAJA, todosDe } from '../auth/permisos';

export interface NavItem {
  label: string;
  path: string;
  icon: IconName;
  /** Permiso (o cualquiera de la lista) requerido para ver la sección. */
  permiso: string | readonly string[];
}

export interface NavSection {
  title: string;
  items: readonly NavItem[];
}

const VENTAS_VER = [PERMISOS.ventaVer, PERMISOS.ventaCrear] as const;
const DEVOLUCIONES_VER = [PERMISOS.ventaVer, PERMISOS.ventaDevolver] as const;
const INVENTARIO = [PERMISOS.inventarioVer, PERMISOS.inventarioMover] as const;

/** Menú lateral agrupado. Las secciones sin ningún ítem permitido no se muestran. */
export const NAV_SECTIONS: readonly NavSection[] = [
  {
    title: 'Ventas',
    items: [
      { label: 'Punto de venta', path: '/pos', icon: 'cart', permiso: PERMISOS.ventaCrear },
      { label: 'Ventas', path: '/ventas', icon: 'receipt', permiso: VENTAS_VER },
      { label: 'Devoluciones', path: '/devoluciones', icon: 'undo', permiso: DEVOLUCIONES_VER },
      { label: 'Turnos de caja', path: '/turnos', icon: 'wallet', permiso: PERMISOS_CAJA },
    ],
  },
  {
    title: 'Compras',
    items: [
      { label: 'Pedidos', path: '/pedidos', icon: 'truck', permiso: PERMISOS.pedidoVer },
      { label: 'Proveedores', path: '/proveedores', icon: 'users', permiso: PERMISOS.proveedorVer },
    ],
  },
  {
    title: 'Inventario',
    items: [
      { label: 'Stock', path: '/inventario', icon: 'box', permiso: INVENTARIO },
      { label: 'Registrar movimiento', path: '/registrar-movimiento', icon: 'upload', permiso: PERMISOS.inventarioMover },
      { label: 'Movimientos', path: '/movimientos', icon: 'activity', permiso: INVENTARIO },
      { label: 'Lotes', path: '/lotes', icon: 'tag', permiso: [MODULOS.catalogo.editar, PERMISOS.inventarioVer] },
    ],
  },
  {
    title: 'Catálogo',
    items: [
      { label: 'Productos', path: '/productos', icon: 'box', permiso: todosDe(MODULOS.catalogo) },
      { label: 'Categorías', path: '/categorias', icon: 'clipboard', permiso: todosDe(MODULOS.catalogo) },
      { label: 'Unidades de medida', path: '/unidades-medida', icon: 'grid', permiso: todosDe(MODULOS.catalogo) },
      { label: 'Lista de precios', path: '/precios', icon: 'dollar', permiso: todosDe(MODULOS.precio) },
    ],
  },
  {
    title: 'Clientes',
    items: [
      { label: 'Clientes', path: '/clientes', icon: 'user', permiso: todosDe(MODULOS.cliente) },
      { label: 'Tipos de cliente', path: '/tipos-cliente', icon: 'tag', permiso: todosDe(MODULOS.cliente) },
      { label: 'Créditos', path: '/creditos', icon: 'dollar', permiso: PERMISOS.creditoGestionar },
    ],
  },
  {
    title: 'Reportes',
    items: [
      { label: 'Ventas', path: '/reportes/ventas', icon: 'activity', permiso: PERMISOS.reporteVentas },
      { label: 'Caja', path: '/reportes/caja', icon: 'wallet', permiso: PERMISOS.reporteCaja },
      { label: 'Inventario', path: '/reportes/inventario', icon: 'box', permiso: PERMISOS.reporteInventario },
      { label: 'Utilidad', path: '/reportes/utilidad', icon: 'dollar', permiso: PERMISOS.reporteUtilidad },
      { label: 'Compras', path: '/reportes/compras', icon: 'truck', permiso: PERMISOS.reporteCompras },
    ],
  },
  {
    title: 'Configuración',
    items: [
      { label: 'Cajas', path: '/cajas', icon: 'grid', permiso: todosDe(MODULOS.caja) },
      { label: 'Depósitos', path: '/depositos', icon: 'location', permiso: todosDe(MODULOS.deposito) },
      { label: 'Métodos de pago', path: '/metodos-pago', icon: 'wallet', permiso: todosDe(MODULOS.metodoPago) },
    ],
  },
  {
    title: 'Administración',
    items: [
      { label: 'Usuarios', path: '/usuarios', icon: 'users', permiso: PERMISOS.usuarioVer },
      { label: 'Roles y permisos', path: '/roles', icon: 'shield', permiso: PERMISOS.rolVer },
    ],
  },
];

/** Lista plana en el orden del menú: la usa la redirección inicial (primera sección accesible). */
export const NAV_ITEMS: readonly NavItem[] = NAV_SECTIONS.flatMap((s) => s.items);
