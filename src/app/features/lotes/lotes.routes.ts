import { Routes } from '@angular/router';

export const LOTES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Lotes', subtitle: 'Costos, vencimientos y descuentos por vencimiento' },
    loadComponent: () => import('./pages/lotes-page').then((m) => m.LotesPage),
  },
];
