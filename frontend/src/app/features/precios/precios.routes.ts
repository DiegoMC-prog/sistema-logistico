import { Routes } from '@angular/router';

export const PRECIOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Lista de precios', subtitle: 'Precios por presentación, tipo de cliente y cantidad' },
    loadComponent: () => import('./pages/precios-page').then((m) => m.PreciosPage),
  },
];
