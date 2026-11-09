import { Routes } from '@angular/router';

export const CREDITOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Créditos', subtitle: 'Clientes con deuda pendiente (cuentas por cobrar)' },
    loadComponent: () => import('./pages/deudores-page').then((m) => m.DeudoresPage),
  },
  {
    path: ':id',
    data: { title: 'Estado de cuenta', subtitle: 'Deuda, ventas a crédito y abonos del cliente' },
    loadComponent: () => import('./pages/estado-cuenta-page').then((m) => m.EstadoCuentaPage),
  },
];
