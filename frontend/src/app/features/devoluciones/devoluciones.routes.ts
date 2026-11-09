import { Routes } from '@angular/router';

export const DEVOLUCIONES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Devoluciones', subtitle: 'Historial de devoluciones y cambios' },
    loadComponent: () => import('./pages/devoluciones-page').then((m) => m.DevolucionesPage),
  },
];
