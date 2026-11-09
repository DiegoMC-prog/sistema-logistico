import { Routes } from '@angular/router';
import { permissionGuard } from '../../core/auth/auth.guards';
import { PERMISOS } from '../../core/auth/permisos';

export const PEDIDOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Pedidos a proveedores', subtitle: 'Compras, envíos y recepciones de mercadería' },
    loadComponent: () => import('./pages/pedidos-page').then((m) => m.PedidosPage),
  },
  {
    path: 'nuevo',
    canActivate: [permissionGuard(PERMISOS.pedidoCrear)],
    data: { title: 'Nuevo pedido', subtitle: 'Se crea como borrador y se puede editar hasta enviarlo' },
    loadComponent: () => import('./pages/pedido-form-page').then((m) => m.PedidoFormPage),
  },
  {
    path: ':id/editar',
    canActivate: [permissionGuard(PERMISOS.pedidoEditar)],
    data: { title: 'Editar pedido', subtitle: 'Solo los borradores se pueden editar' },
    loadComponent: () => import('./pages/pedido-form-page').then((m) => m.PedidoFormPage),
  },
  {
    path: ':id',
    data: { title: 'Detalle del pedido', subtitle: 'Líneas, progreso de recepción y recepciones' },
    loadComponent: () => import('./pages/pedido-detalle-page').then((m) => m.PedidoDetallePage),
  },
];
