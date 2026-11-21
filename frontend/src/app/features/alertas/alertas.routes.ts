import { Routes } from '@angular/router';

export const ALERTAS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Alertas', subtitle: 'Lotes por vencer, lotes vencidos con stock y productos bajo el mínimo' },
    loadComponent: () => import('./pages/alertas-page').then((m) => m.AlertasPage),
  },
];
