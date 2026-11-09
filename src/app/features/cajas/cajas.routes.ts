import { Routes } from '@angular/router';

export const CAJAS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Cajas', subtitle: 'Puntos de cobro asociados a un depósito' },
    loadComponent: () => import('./pages/cajas-page').then((m) => m.CajasPage),
  },
];
