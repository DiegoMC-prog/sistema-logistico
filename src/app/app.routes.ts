import { Routes } from '@angular/router';
import { authGuard, guestGuard, homeGuard, permissionGuard } from './core/auth/auth.guards';
import { PERMISOS, PERMISOS_CAJA } from './core/auth/permisos';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/pages/login-page').then((m) => m.LoginPage),
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
      { path: '', pathMatch: 'full', canActivate: [homeGuard], children: [] },
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
        canActivate: [permissionGuard([PERMISOS.catalogo, PERMISOS.inventarioVer])],
        loadChildren: () => import('./features/lotes/lotes.routes').then((m) => m.LOTES_ROUTES),
      },
      {
        path: 'turnos',
        canActivate: [permissionGuard(PERMISOS_CAJA)],
        loadChildren: () => import('./features/turnos/turnos.routes').then((m) => m.TURNOS_ROUTES),
      },
      {
        path: 'cajas',
        canActivate: [permissionGuard(PERMISOS.cajaGestionar)],
        loadChildren: () => import('./features/cajas/cajas.routes').then((m) => m.CAJAS_ROUTES),
      },
      {
        path: 'clientes',
        canActivate: [permissionGuard(PERMISOS.cliente)],
        loadChildren: () => import('./features/clientes/clientes.routes').then((m) => m.CLIENTES_ROUTES),
      },
      {
        path: 'tipos-cliente',
        canActivate: [permissionGuard(PERMISOS.cliente)],
        loadChildren: () => import('./features/tipos-cliente/tipos-cliente.routes').then((m) => m.TIPOS_CLIENTE_ROUTES),
      },
      {
        path: 'precios',
        canActivate: [permissionGuard(PERMISOS.precio)],
        loadChildren: () => import('./features/precios/precios.routes').then((m) => m.PRECIOS_ROUTES),
      },
      {
        path: 'metodos-pago',
        canActivate: [permissionGuard(PERMISOS.metodoPago)],
        loadChildren: () => import('./features/metodos-pago/metodos-pago.routes').then((m) => m.METODOS_PAGO_ROUTES),
      },
      {
        path: 'productos',
        canActivate: [permissionGuard(PERMISOS.catalogo)],
        loadChildren: () => import('./features/productos/productos.routes').then((m) => m.PRODUCTOS_ROUTES),
      },
      {
        path: 'categorias',
        canActivate: [permissionGuard(PERMISOS.catalogo)],
        loadChildren: () => import('./features/categorias/categorias.routes').then((m) => m.CATEGORIAS_ROUTES),
      },
      {
        path: 'unidades-medida',
        canActivate: [permissionGuard(PERMISOS.catalogo)],
        loadChildren: () =>
          import('./features/unidades-medida/unidades-medida.routes').then((m) => m.UNIDADES_MEDIDA_ROUTES),
      },
      {
        path: 'depositos',
        canActivate: [permissionGuard(PERMISOS.deposito)],
        loadChildren: () => import('./features/depositos/depositos.routes').then((m) => m.DEPOSITOS_ROUTES),
      },
      {
        path: 'usuarios',
        canActivate: [permissionGuard(PERMISOS.usuario)],
        loadChildren: () => import('./features/usuarios/usuarios.routes').then((m) => m.USUARIOS_ROUTES),
      },
      {
        path: 'roles',
        canActivate: [permissionGuard(PERMISOS.rol)],
        loadChildren: () => import('./features/roles/roles.routes').then((m) => m.ROLES_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
