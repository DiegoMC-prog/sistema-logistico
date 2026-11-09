import { IconName } from '../../shared/ui/icon';
import { PERMISOS, PERMISOS_CAJA } from '../auth/permisos';

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
    title: 'Inventario',
    items: [
      { label: 'Stock', path: '/inventario', icon: 'box', permiso: INVENTARIO },
      { label: 'Registrar movimiento', path: '/registrar-movimiento', icon: 'upload', permiso: PERMISOS.inventarioMover },
      { label: 'Movimientos', path: '/movimientos', icon: 'activity', permiso: INVENTARIO },
      { label: 'Lotes', path: '/lotes', icon: 'tag', permiso: [PERMISOS.catalogo, PERMISOS.inventarioVer] },
    ],
  },
  {
    title: 'Catálogo',
    items: [
      { label: 'Productos', path: '/productos', icon: 'box', permiso: PERMISOS.catalogo },
      { label: 'Categorías', path: '/categorias', icon: 'clipboard', permiso: PERMISOS.catalogo },
      { label: 'Unidades de medida', path: '/unidades-medida', icon: 'grid', permiso: PERMISOS.catalogo },
      { label: 'Lista de precios', path: '/precios', icon: 'dollar', permiso: PERMISOS.precio },
    ],
  },
  {
    title: 'Clientes',
    items: [
      { label: 'Clientes', path: '/clientes', icon: 'user', permiso: PERMISOS.cliente },
      { label: 'Tipos de cliente', path: '/tipos-cliente', icon: 'tag', permiso: PERMISOS.cliente },
    ],
  },
  {
    title: 'Configuración',
    items: [
      { label: 'Cajas', path: '/cajas', icon: 'grid', permiso: PERMISOS.cajaGestionar },
      { label: 'Depósitos', path: '/depositos', icon: 'location', permiso: PERMISOS.deposito },
      { label: 'Métodos de pago', path: '/metodos-pago', icon: 'wallet', permiso: PERMISOS.metodoPago },
    ],
  },
  {
    title: 'Administración',
    items: [
      { label: 'Usuarios', path: '/usuarios', icon: 'users', permiso: PERMISOS.usuario },
      { label: 'Roles y permisos', path: '/roles', icon: 'shield', permiso: PERMISOS.rol },
    ],
  },
];

/** Lista plana en el orden del menú: la usa la redirección inicial (primera sección accesible). */
export const NAV_ITEMS: readonly NavItem[] = NAV_SECTIONS.flatMap((s) => s.items);
