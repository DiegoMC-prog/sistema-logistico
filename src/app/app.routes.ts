import { Routes } from '@angular/router';
import { authGuard, guestGuard, permissionGuard } from './core/auth/auth.guards';
import { MODULOS, PERMISOS, PERMISOS_CAJA, todosDe } from './core/auth/permisos';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/pages/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'olvide-password',
    loadComponent: () => import('./features/auth/pages/olvide-password-page').then((m) => m.OlvidePasswordPage),
  },
  {
    path: 'restablecer',
    loadComponent: () => import('./features/auth/pages/restablecer-password-page').then((m) => m.RestablecerPasswordPage),
  },
  {
    path: 'cambiar-password',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/pages/cambiar-password-page').then((m) => m.CambiarPasswordPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./core/layout/shell').then((m) => m.Shell),
    children: [
      // Inicio: redirige a la primera sección permitida para el usuario.
      {
        path: '',
        pathMatch: 'full',
        data: { title: 'Inicio', subtitle: 'Resumen de la operación' },
        loadComponent: () => import('./features/inicio/pages/inicio-page').then((m) => m.InicioPage),
      },
      {
        path: 'sin-acceso',
        data: { title: 'Sin acceso', subtitle: '' },
        loadComponent: () => import('./features/auth/pages/sin-acceso-page').then((m) => m.SinAccesoPage),
      },
      {
        path: 'pos',
        canActivate: [permissionGuard(PERMISOS.ventaCrear)],
        loadChildren: () => import('./features/ventas/ventas.routes').then((m) => m.POS_ROUTES),
      },
      {
        path: 'ventas',
        canActivate: [permissionGuard([PERMISOS.ventaVer, PERMISOS.ventaCrear])],
        loadChildren: () => import('./features/ventas/ventas.routes').then((m) => m.VENTAS_ROUTES),
      },
      {
        path: 'devoluciones',
        canActivate: [permissionGuard([PERMISOS.ventaVer, PERMISOS.ventaDevolver])],
        loadChildren: () => import('./features/devoluciones/devoluciones.routes').then((m) => m.DEVOLUCIONES_ROUTES),
      },
      {
        path: 'inventario',
        canActivate: [permissionGuard([PERMISOS.inventarioVer, PERMISOS.inventarioMover])],
        loadChildren: () => import('./features/inventario/inventario.routes').then((m) => m.STOCK_ROUTES),
      },
      {
        path: 'registrar-movimiento',
        canActivate: [permissionGuard(PERMISOS.inventarioMover)],
        loadChildren: () => import('./features/inventario/inventario.routes').then((m) => m.OPERACIONES_ROUTES),
      },
      {
        path: 'movimientos',
        canActivate: [permissionGuard([PERMISOS.inventarioVer, PERMISOS.inventarioMover])],
        loadChildren: () => import('./features/inventario/inventario.routes').then((m) => m.MOVIMIENTOS_ROUTES),
      },
      {
        path: 'lotes',
        canActivate: [permissionGuard([MODULOS.catalogo.editar, PERMISOS.inventarioVer])],
        loadChildren: () => import('./features/lotes/lotes.routes').then((m) => m.LOTES_ROUTES),
      },
      {
        path: 'pedidos',
        canActivate: [permissionGuard(PERMISOS.pedidoVer)],
        loadChildren: () => import('./features/pedidos/pedidos.routes').then((m) => m.PEDIDOS_ROUTES),
      },
      {
        path: 'proveedores',
        canActivate: [permissionGuard(PERMISOS.proveedorVer)],
        loadChildren: () => import('./features/proveedores/proveedores.routes').then((m) => m.PROVEEDORES_ROUTES),
      },
      {
        path: 'reportes',
        loadChildren: () => import('./features/reportes/reportes.routes').then((m) => m.REPORTES_ROUTES),
      },
      {
        path: 'turnos',
        canActivate: [permissionGuard(PERMISOS_CAJA)],
        loadChildren: () => import('./features/turnos/turnos.routes').then((m) => m.TURNOS_ROUTES),
      },
      {
        path: 'cajas',
        canActivate: [permissionGuard(todosDe(MODULOS.caja))],
        loadChildren: () => import('./features/cajas/cajas.routes').then((m) => m.CAJAS_ROUTES),
      },
      {
        path: 'clientes',
        canActivate: [permissionGuard(todosDe(MODULOS.cliente))],
        loadChildren: () => import('./features/clientes/clientes.routes').then((m) => m.CLIENTES_ROUTES),
      },
      {
        path: 'tipos-cliente',
        canActivate: [permissionGuard(todosDe(MODULOS.cliente))],
        loadChildren: () => import('./features/tipos-cliente/tipos-cliente.routes').then((m) => m.TIPOS_CLIENTE_ROUTES),
      },
      {
        path: 'precios',
        canActivate: [permissionGuard(todosDe(MODULOS.precio))],
        loadChildren: () => import('./features/precios/precios.routes').then((m) => m.PRECIOS_ROUTES),
      },
      {
        path: 'metodos-pago',
        canActivate: [permissionGuard(todosDe(MODULOS.metodoPago))],
        loadChildren: () => import('./features/metodos-pago/metodos-pago.routes').then((m) => m.METODOS_PAGO_ROUTES),
      },
      {
        path: 'productos',
        canActivate: [permissionGuard(todosDe(MODULOS.catalogo))],
        loadChildren: () => import('./features/productos/productos.routes').then((m) => m.PRODUCTOS_ROUTES),
      },
      {
        path: 'categorias',
        canActivate: [permissionGuard(todosDe(MODULOS.catalogo))],
        loadChildren: () => import('./features/categorias/categorias.routes').then((m) => m.CATEGORIAS_ROUTES),
      },
      {
        path: 'unidades-medida',
        canActivate: [permissionGuard(todosDe(MODULOS.catalogo))],
        loadChildren: () =>
          import('./features/unidades-medida/unidades-medida.routes').then((m) => m.UNIDADES_MEDIDA_ROUTES),
      },
      {
        path: 'depositos',
        canActivate: [permissionGuard(todosDe(MODULOS.deposito))],
        loadChildren: () => import('./features/depositos/depositos.routes').then((m) => m.DEPOSITOS_ROUTES),
      },
      {
        path: 'usuarios',
        canActivate: [permissionGuard(PERMISOS.usuarioVer)],
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
      {
        path: 'roles',
        canActivate: [permissionGuard(PERMISOS.rolVer)],
        loadChildren: () => import('./features/roles/roles.routes').then((m) => m.ROLES_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
